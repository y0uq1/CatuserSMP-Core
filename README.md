# CatuserSMP-Core

CatuserSMP 服务器的核心插件框架，基于 **Paper 1.21.11** / **Java 21**。

它不提供具体玩法，而是把每个插件都会重复实现的东西（配置、文本、模块开关、命令路由、调度）收敛成一层可复用的骨架，
后续的玩法（经济、领地、副本、活动……）以 **模块（Module）** 或 **子命令（SubCommand）** 的形式增量接入。

---

## 环境要求

| 项目 | 版本 |
| --- | --- |
| JDK | 21（本机路径 `C:\Program Files\Zulu\zulu-21`） |
| 服务端 | Paper 1.21.11 |
| 构建 | Gradle Wrapper 9.8.2（仓库内已自带，无需本机安装 Gradle） |
| 依赖 | `io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT`（`compileOnly`） |

## 构建

```powershell
$env:JAVA_HOME = 'C:\Program Files\Zulu\zulu-21'
.\gradlew.bat build
```

产物：`build/libs/CatuserSMP-Core-1.0.0.jar`，直接放入服务端 `plugins/` 目录即可。

其他常用命令：

```powershell
.\gradlew.bat clean build     # 全量构建
.\gradlew.bat jar             # 只打包
.\gradlew.bat compileJava     # 只编译（最快的问题定位方式）
```

> `gradlew.bat` 首次执行会下载 Gradle 发行包与 `paper-api` 依赖，需要联网。

## 目录结构

```
CatuserSMP-Core/
├── build.gradle.kts                 构建脚本（Java 21 工具链、Paper 仓库、版本注入）
├── settings.gradle.kts
├── gradle.properties
├── gradlew / gradlew.bat            跨平台 Wrapper 启动脚本
├── gradle/wrapper/
└── src/main/
    ├── java/com/catusersmp/core/
    │   ├── CatuserSMPCorePlugin.java        入口：装配各服务与模块（唯一 extends JavaPlugin 的类）
    │   ├── command/
    │   │   ├── SubCommand.java              子命令契约
    │   │   ├── CommandRegistry.java         分发 + 权限 + Tab 补全
    │   │   └── impl/                        help / info / reload
    │   ├── config/
    │   │   ├── ConfigService.java           类型化读取 config.yml
    │   │   └── Messages.java                MiniMessage 文本层（messages.yml）
    │   ├── module/
    │   │   ├── Module.java                  可开关特性基类（实现 Listener 自动注册/注销）
    │   │   └── ModuleManager.java           注册、按配置启停、逆序关闭
    │   └── util/
    │       ├── Text.java                    MiniMessage 解析与降级
    │       └── Tasks.java                   同步/异步/延时/循环调度封装
    └── resources/
        ├── plugin.yml                       插件描述符（版本由构建注入）
        ├── config.yml                       主配置
        └── messages.yml                     全部玩家可见文本
```

## 内置命令

| 命令 | 权限 | 默认 | 说明 |
| --- | --- | --- | --- |
| `/catuser help` | — | 所有人 | 列出当前发送者可见的子命令 |
| `/catuser info` | `catusersmp.command.info` | OP | 插件 / 服务端 / Java / 模块状态 |
| `/catuser reload` | `catusersmp.command.reload` | OP | 重载 config.yml、messages.yml 并重算模块 |

别名：`/csmp`、`/catcore`、`/catuser ?`、`/catuser rl`。

## 扩展指南

### 新增一个模块

模块适合「能被配置整体开关的一整块玩法」。

```java
public final class EconomyModule extends Module implements Listener {

    public EconomyModule(CatuserSMPCorePlugin plugin) {
        super(plugin);
    }

    @Override
    public String id() {
        return "economy";          // 对应 config.yml 中的 modules.economy.enabled
    }

    @Override
    protected void onEnable() {
        // 初始化存储、缓存、定时任务等
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.messages().send(event.getPlayer(), "economy.balance");
    }
}
```

在 `CatuserSMPCorePlugin#onEnable` 中注册即可：

```java
this.moduleManager.register(new EconomyModule(this));
```

`Module` 已处理的事情：`enabled` 状态、`onEnable` / `onDisable` 异常隔离、实现 `Listener` 时自动注册与注销监听器、
`debug(...)` 按 `settings.debug` 输出日志、`/catuser reload` 时先逆序关闭再按新配置重启。

### 新增一个子命令

```java
public final class BalanceCommand implements SubCommand {

    private final CatuserSMPCorePlugin plugin;

    public BalanceCommand(CatuserSMPCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String name() {
        return "balance";
    }

    @Override
    public List<String> aliases() {
        return List.of("bal");
    }

    @Override
    public String permission() {
        return "catusersmp.command.balance";
    }

    @Override
    public String description() {
        return "查看余额";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        // args 已去掉 "balance" 这一段
        return true;
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        return List.of();
    }
}
```

注册：`this.commandRegistry.register(new BalanceCommand(this));`
新权限节点记得同步写进 `plugin.yml` 的 `permissions` 段。

## 配置与文本

- `config.yml`：`settings.debug` 控制调试日志；`modules.<id>.enabled` 控制模块开关。
- `messages.yml`：全部 MiniMessage 文本，支持 `gradient`、`<click>`、`<hover>` 等标签；
  每条消息都可使用 `<prefix>`；`Messages#component(key, Placeholder.unparsed("player", name))` 传入占位符。
- 两处文本都支持 `/catuser reload` 热重载，缺失的键会自动回退到 JAR 内置默认值。

## 说明

- 只对 Paper 1.21.11 编译（`api-version: '1.21'`），未做 Spigot/BungeeCord 兼容。
- 使用 Bukkit 命令执行器而非 Brigadier：实现简单、Tab 补全完全可控；如需原生命令树可后续在 `bindCommand` 处替换。
- 调度统一走 `Tasks`，若之后要迁移 Folia，只需要替换这一层的实现。
