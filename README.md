# midjourney-proxy

代理 MidJourney 的discord频道，实现api形式调用AI绘图
魔改自https://github.com/novicezk/midjourney-proxy
谢谢原作者的伟大付出

## 主要功能
- [x] 支持 Imagine 指令和相关动作
- [x] Imagine 时支持添加图片base64，作为垫图
- [x] 支持 Blend(图片混合)、Describe(图生文) 指令
- [x] 支持Upscale(2x),Upscale(4x)放大分辨率命令
- [x] 支持Zoom Out 1.5x，Zoom Out 2x缩放命令
- [x] 支持Vary(Subtle)，Vary(Strong)轻微变化和强烈变化命令
- [x] 支持上下左右延伸变化命令
- [x] 支持任务实时进度
- [x] 支持任务内存存储和Redis存储
- [x] 支持中文prompt翻译，需配置百度翻译或gpt
- [x] prompt 敏感词预检测，支持覆盖调整
- [x] user-token 连接 wss，可以获取错误信息和完整功能
- [x] 支持多账号配置，每个账号可设置对应的任务队列

## 使用前提
1. 注册并订阅 MidJourney，创建`自己的服务器和频道`，参考 https://docs.midjourney.com/docs/quick-start
2. 获取用户Token、服务器ID、频道ID：[获取方式](./docs/discord-params.md)


## 本地开发
- 依赖java17和maven
- 更改配置项: 修改src/main/application.yml
- 项目运行: 启动ProxyApplication的main函数

## 配置项
- mj.accounts: 参考 [账号池配置](./docs/config.md#%E8%B4%A6%E5%8F%B7%E6%B1%A0%E9%85%8D%E7%BD%AE%E5%8F%82%E8%80%83)
- mj.task-store.type: 任务存储方式，默认in_memory(内存\重启后丢失)，可选redis
- mj.task-store.timeout: 任务存储过期时间，过期后删除，默认30天
- mj.api-secret: 接口密钥，为空不启用鉴权；调用接口时需要加请求头 mj-api-secret
- mj.translate-way: 中文prompt翻译成英文的方式，可选null(默认)、baidu、gpt
- 更多配置查看 [配置项](./docs/config.md)

## 相关文档
1. [API接口说明](./docs/api.md)

## 注意事项
1. 作图频繁等行为，可能会触发midjourney账号警告，请谨慎使用