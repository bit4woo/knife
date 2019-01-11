[![Open Source Love](https://badges.frapsoft.com/os/v1/open-source.svg?v=103)](https://github.com/ellerbrock/open-source-badges/)  [![MIT Licence](https://badges.frapsoft.com/os/mit/mit.svg?v=103)](https://opensource.org/licenses/mit-license.php)

### [knife](https://github.com/bit4woo/knife)

A extension that add some small function[ one key to update cookie, one key add host to scope] to right click context menu.



### Authors

[bit4woo](https://github.com/bit4woo)

### Functions

##### Menus (simple is beautiful,some menu deleted)

1. update cookie

   update current request cookie which in repeater within the latest cookie fetched from proxy history.

2. add host to scope

   add current request host to burp scope not URL.

3. update header

  update Header that likes token,authorization .

4. open with browser

  open URL of current request or selected URL with browser you configured.

5. hackbar++

   insert payload of [Hackbar](https://github.com/d3vilbug/HackBar) or self-configured to current request


##### Tab

1. U2C

   convert Unicode To Chinese (eg. `\u4e2d\u6587`-->`中文`) 

##### request edit

1. auto remove some headers , eg. Last-Modified,If-Modified-Since,If-None-Match. it's for all requests
2. auto add/update/append some headers, you can control which requests to enable for.

### Screen shot

update cookie:

![updatecookie](img/updatecookie.png)

update header：

![](img/updateheader.png)

open with browser:

![openwithbrowser](img/openwithbrowser.gif)

payload(Hackbar++):

![insertpayload](img/insertpayload.gif)

any issue, advice, suggestion are welcome, Thanks！
