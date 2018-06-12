### [knife](https://github.com/bit4woo/knife)

A extension that add some small function to context menu；

一个将有用的小功能加入到右键菜单的burp suite插件。

### Author作者

[bit4](https://github.com/bit4woo)

### 功能说明

目前有四个菜单：

1. copy this cookie

   尝试复制当前请求中的cookie值到剪贴板，如果当前请求没有cookie值，将不做操作。

2. get lastest cookie

   从proxy history中获取与当前域的最新cookie值。个人觉得这个很有有用，特别是当repeater等请求中的cookie过期，而又需要重放复现时。感谢cf_hb师傅的idea。

3. add host to scope

   将当前请求的host添加到burp的scope中，我们常常需要的时将整个网站加到scope而不是一个具体的URL。

4. U2C

   尝试对选中的内容进行【Unicode转中文的操作】，只有当选中的内容时response是才会显示该菜单。

![U2C](.\img\U2C.png)



如有任何小的改进和想要实现的小功能点，都欢迎提交给我，谢谢！