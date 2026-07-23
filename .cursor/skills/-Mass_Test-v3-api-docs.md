# Mass_Test1 API文档


**简介**:Mass_Test1 API文档


**HOST**:http://localhost:100/Mass_Test


**联系人**:


**Version**:v1.0


**接口路径**:/Mass_Test/v3/api-docs


[TOC]






# 通知模板


## 更新模板


**接口地址**:`/Mass_Test/notice-template/update`


**请求方式**:`PUT`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>body 中 templateName 为创建时返回的编码</p>



**请求示例**:


```javascript
{
  "templateName": "",
  "title": "",
  "content": "",
  "categoryId": 0
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|noticeTemplateDTO|NoticeTemplateDTO|body|true|NoticeTemplateDTO|NoticeTemplateDTO|
|&emsp;&emsp;templateName|||false|string||
|&emsp;&emsp;title|||true|string||
|&emsp;&emsp;content|||true|string||
|&emsp;&emsp;categoryId|||false|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 新增模板


**接口地址**:`/Mass_Test/notice-template/save`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>无需传 templateName，服务端生成并返回</p>



**请求示例**:


```javascript
{
  "templateName": "",
  "title": "",
  "content": "",
  "categoryId": 0
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|noticeTemplateDTO|NoticeTemplateDTO|body|true|NoticeTemplateDTO|NoticeTemplateDTO|
|&emsp;&emsp;templateName|||false|string||
|&emsp;&emsp;title|||true|string||
|&emsp;&emsp;content|||true|string||
|&emsp;&emsp;categoryId|||false|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 模板列表


**接口地址**:`/Mass_Test/notice-template/list`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|param||query|true|object||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 模板详情


**接口地址**:`/Mass_Test/notice-template/detail`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|templateName||query|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 删除模板


**接口地址**:`/Mass_Test/notice-template/delete`


**请求方式**:`DELETE`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>被引用时改为停用</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|templateName||query|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


# 通知


## 撤回通知


**接口地址**:`/Mass_Test/notice-info/withdraw/{id}`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id||path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 上传通知附件


**接口地址**:`/Mass_Test/notice-info/upload`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|file||query|true|file||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 发送通知


**接口地址**:`/Mass_Test/notice-info/send`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>支持立即发送、定时发送或保存草稿</p>



**请求示例**:


```javascript
{
  "title": "",
  "content": "",
  "categoryId": 0,
  "receiverType": 0,
  "receiverValues": "",
  "importance": 0,
  "urgency": 0,
  "needConfirm": true,
  "scheduledPublishTime": "",
  "pinned": true,
  "pinExpireAt": "",
  "longTermVisible": true,
  "attachments": [],
  "attachmentMinLevel": 0,
  "templateId": 0,
  "draft": true
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|noticeSendDTO|NoticeSendDTO|body|true|NoticeSendDTO|NoticeSendDTO|
|&emsp;&emsp;title|||true|string||
|&emsp;&emsp;content|||true|string||
|&emsp;&emsp;categoryId|||true|integer(int64)||
|&emsp;&emsp;receiverType|||true|integer(int32)||
|&emsp;&emsp;receiverValues|||true|string||
|&emsp;&emsp;importance|||false|integer(int32)||
|&emsp;&emsp;urgency|||false|integer(int32)||
|&emsp;&emsp;needConfirm|||true|boolean||
|&emsp;&emsp;scheduledPublishTime|||false|string(date-time)||
|&emsp;&emsp;pinned|||false|boolean||
|&emsp;&emsp;pinExpireAt|||false|string(date-time)||
|&emsp;&emsp;longTermVisible|||true|boolean||
|&emsp;&emsp;attachments|||false|array|string|
|&emsp;&emsp;attachmentMinLevel|||false|integer(int32)||
|&emsp;&emsp;templateId|||false|integer(int64)||
|&emsp;&emsp;draft|||false|boolean||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 手动发布草稿


**接口地址**:`/Mass_Test/notice-info/publish/{id}`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id||path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 保存草稿


**接口地址**:`/Mass_Test/notice-info/draft`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "title": "",
  "content": "",
  "categoryId": 0,
  "receiverType": 0,
  "receiverValues": "",
  "importance": 0,
  "urgency": 0,
  "needConfirm": true,
  "scheduledPublishTime": "",
  "pinned": true,
  "pinExpireAt": "",
  "longTermVisible": true,
  "attachments": [],
  "attachmentMinLevel": 0,
  "templateId": 0,
  "draft": true
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|noticeSendDTO|NoticeSendDTO|body|true|NoticeSendDTO|NoticeSendDTO|
|&emsp;&emsp;title|||true|string||
|&emsp;&emsp;content|||true|string||
|&emsp;&emsp;categoryId|||true|integer(int64)||
|&emsp;&emsp;receiverType|||true|integer(int32)||
|&emsp;&emsp;receiverValues|||true|string||
|&emsp;&emsp;importance|||false|integer(int32)||
|&emsp;&emsp;urgency|||false|integer(int32)||
|&emsp;&emsp;needConfirm|||true|boolean||
|&emsp;&emsp;scheduledPublishTime|||false|string(date-time)||
|&emsp;&emsp;pinned|||false|boolean||
|&emsp;&emsp;pinExpireAt|||false|string(date-time)||
|&emsp;&emsp;longTermVisible|||true|boolean||
|&emsp;&emsp;attachments|||false|array|string|
|&emsp;&emsp;attachmentMinLevel|||false|integer(int32)||
|&emsp;&emsp;templateId|||false|integer(int64)||
|&emsp;&emsp;draft|||false|boolean||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 确认阅读


**接口地址**:`/Mass_Test/notice-info/confirm/{id}`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id||path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 已读-确认统计


**接口地址**:`/Mass_Test/notice-info/stats/{id}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>仅发布人或有权限管理员可查看</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id||path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 我发送的通知


**接口地址**:`/Mass_Test/notice-info/sent`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|param||query|true|object||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 我的收件箱


**接口地址**:`/Mass_Test/notice-info/inbox`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|param||query|true|object||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 通知详情


**接口地址**:`/Mass_Test/notice-info/detail/{id}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>进入详情即记录已读</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id||path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


# 角色管理


## 更新角色


**接口地址**:`/Mass_Test/sys-role/updateSysRole`


**请求方式**:`PUT`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>管理员及以上可调用，权限校验同批量更新</p>



**请求示例**:


```javascript
{
  "id": 0,
  "roleName": "",
  "roleCode": "",
  "roleLevel": 0,
  "dataScope": 0,
  "description": "",
  "status": 0,
  "createTime": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|sysRole|SysRole|body|true|SysRole|SysRole|
|&emsp;&emsp;id|||false|integer(int64)||
|&emsp;&emsp;roleName|||false|string||
|&emsp;&emsp;roleCode|||false|string||
|&emsp;&emsp;roleLevel|||false|integer(int32)||
|&emsp;&emsp;dataScope|||false|integer(int32)||
|&emsp;&emsp;description|||false|string||
|&emsp;&emsp;status|||false|integer(int32)||
|&emsp;&emsp;createTime|||false|string(date-time)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 批量更新角色


**接口地址**:`/Mass_Test/sys-role/updateSysRoleBatc`


**请求方式**:`PUT`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>管理员及以上可调用，仅能修改低于或等于自身等级的角色，不能提权</p>



**请求示例**:


```javascript
[
  {
    "id": 0,
    "roleName": "",
    "roleCode": "",
    "roleLevel": 0,
    "dataScope": 0,
    "description": "",
    "status": 0,
    "createTime": ""
  }
]
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|sysRoles|SysRole|body|true|array|SysRole|
|&emsp;&emsp;id|||false|integer(int64)||
|&emsp;&emsp;roleName|||false|string||
|&emsp;&emsp;roleCode|||false|string||
|&emsp;&emsp;roleLevel|||false|integer(int32)||
|&emsp;&emsp;dataScope|||false|integer(int32)||
|&emsp;&emsp;description|||false|string||
|&emsp;&emsp;status|||false|integer(int32)||
|&emsp;&emsp;createTime|||false|string(date-time)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 新增角色


**接口地址**:`/Mass_Test/sys-role/addSysRole`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>管理员及以上可调用，不能创建高于自身等级的角色</p>



**请求示例**:


```javascript
{
  "id": 0,
  "roleName": "",
  "roleCode": "",
  "roleLevel": 0,
  "dataScope": 0,
  "description": "",
  "status": 0,
  "createTime": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|sysRole|SysRole|body|true|SysRole|SysRole|
|&emsp;&emsp;id|||false|integer(int64)||
|&emsp;&emsp;roleName|||false|string||
|&emsp;&emsp;roleCode|||false|string||
|&emsp;&emsp;roleLevel|||false|integer(int32)||
|&emsp;&emsp;dataScope|||false|integer(int32)||
|&emsp;&emsp;description|||false|string||
|&emsp;&emsp;status|||false|integer(int32)||
|&emsp;&emsp;createTime|||false|string(date-time)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 分页查询角色


**接口地址**:`/Mass_Test/sys-role/listSysRole`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>统一查询入口，自动按权限等级过滤（role_level &gt;= 当前用户等级）</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|param||query|true|object||
|sysRole||query|true|SysRole|SysRole|
|&emsp;&emsp;id|||false|integer(int64)||
|&emsp;&emsp;roleName|||false|string||
|&emsp;&emsp;roleCode|||false|string||
|&emsp;&emsp;roleLevel|||false|integer(int32)||
|&emsp;&emsp;dataScope|||false|integer(int32)||
|&emsp;&emsp;description|||false|string||
|&emsp;&emsp;status|||false|integer(int32)||
|&emsp;&emsp;createTime|||false|string(date-time)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 查询角色详情


**接口地址**:`/Mass_Test/sys-role/detailSysRole/{id}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>受等级限制，只能查看低于或等于自身等级的角色</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id||path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 根据ID批量删除角色


**接口地址**:`/Mass_Test/sys-role/deleteSysRole`


**请求方式**:`DELETE`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>管理员及以上可调用，逐条校验权限</p>



**请求示例**:


```javascript
[]
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|integers|integer|body|true|array||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 根据ID删除角色


**接口地址**:`/Mass_Test/sys-role/deleteSysRole/{id}`


**请求方式**:`DELETE`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>管理员及以上可调用，不能删除高于自身等级的角色，也不能删除自己拥有的角色</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id||path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


# 活动签到


## 查询签到配置


**接口地址**:`/Mass_Test/activity-sign/config`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>参与人查看签到点信息</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|activityNo||query|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 配置签到


**接口地址**:`/Mass_Test/activity-sign/config`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>活动负责人配置签到规则</p>



**请求示例**:


```javascript
{
  "activityNo": "",
  "signMode": 0,
  "signStartTime": "",
  "signEndTime": "",
  "signRadius": 0,
  "enableCheckout": true,
  "centerLatitude": 0,
  "centerLongitude": 0
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|signConfigDTO|SignConfigDTO|body|true|SignConfigDTO|SignConfigDTO|
|&emsp;&emsp;activityNo|||true|string||
|&emsp;&emsp;signMode|||true|integer(int32)||
|&emsp;&emsp;signStartTime|||true|string(date-time)||
|&emsp;&emsp;signEndTime|||true|string(date-time)||
|&emsp;&emsp;signRadius|||false|integer(int32)||
|&emsp;&emsp;enableCheckout|||true|boolean||
|&emsp;&emsp;centerLatitude|||false|number||
|&emsp;&emsp;centerLongitude|||false|number||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 更新签到配置


**接口地址**:`/Mass_Test/activity-sign/config`


**请求方式**:`PUT`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>活动负责人或指导老师</p>



**请求示例**:


```javascript
{
  "activityNo": "",
  "signMode": 0,
  "signStartTime": "",
  "signEndTime": "",
  "signRadius": 0,
  "enableCheckout": true,
  "centerLatitude": 0,
  "centerLongitude": 0
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|signConfigDTO|SignConfigDTO|body|true|SignConfigDTO|SignConfigDTO|
|&emsp;&emsp;activityNo|||true|string||
|&emsp;&emsp;signMode|||true|integer(int32)||
|&emsp;&emsp;signStartTime|||true|string(date-time)||
|&emsp;&emsp;signEndTime|||true|string(date-time)||
|&emsp;&emsp;signRadius|||false|integer(int32)||
|&emsp;&emsp;enableCheckout|||true|boolean||
|&emsp;&emsp;centerLatitude|||false|number||
|&emsp;&emsp;centerLongitude|||false|number||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 签到


**接口地址**:`/Mass_Test/activity-sign/sign`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>定位或扫码签到</p>



**请求示例**:


```javascript
{
  "signMethod": 0,
  "latitude": 0,
  "longitude": 0,
  "address": "",
  "qrToken": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|activityNo||query|true|string||
|signActionDTO|SignActionDTO|body|true|SignActionDTO|SignActionDTO|
|&emsp;&emsp;signMethod|||false|integer(int32)||
|&emsp;&emsp;latitude|||false|number||
|&emsp;&emsp;longitude|||false|number||
|&emsp;&emsp;address|||false|string||
|&emsp;&emsp;qrToken|||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 签退


**接口地址**:`/Mass_Test/activity-sign/checkout`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|activityNo||query|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 补签审批


**接口地址**:`/Mass_Test/activity-sign/approve/{applyId}`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "approved": true,
  "opinion": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|applyId||path|true|integer(int64)||
|makeupApproveDTO|MakeupApproveDTO|body|true|MakeupApproveDTO|MakeupApproveDTO|
|&emsp;&emsp;approved|||true|boolean||
|&emsp;&emsp;opinion|||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 申请补签


**接口地址**:`/Mass_Test/activity-sign/apply`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>社长为成员发起</p>



**请求示例**:


```javascript
{
  "username": "",
  "reasonType": 0,
  "reasonDetail": "",
  "attachment": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|activityNo||query|true|string||
|makeupApplyDTO|MakeupApplyDTO|body|true|MakeupApplyDTO|MakeupApplyDTO|
|&emsp;&emsp;username|||true|string||
|&emsp;&emsp;reasonType|||true|integer(int32)||
|&emsp;&emsp;reasonDetail|||true|string||
|&emsp;&emsp;attachment|||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 手动签到


**接口地址**:`/Mass_Test/activity-sign/admin/sign`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>管理员后台标记签到</p>



**请求示例**:


```javascript
{
  "username": "",
  "address": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|activityNo||query|true|string||
|adminSignDTO|AdminSignDTO|body|true|AdminSignDTO|AdminSignDTO|
|&emsp;&emsp;username|||true|string||
|&emsp;&emsp;address|||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 签到统计


**接口地址**:`/Mass_Test/activity-sign/stats`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|activityNo||query|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 签到明细列表


**接口地址**:`/Mass_Test/activity-sign/list`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|activityNo||query|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


# 门户管理


## 上传通知封面


**接口地址**:`/Mass_Test/portal/admin/upload/notice-cover`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|noticeNo||query|true|string||
|file||query|true|file||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 上传社团 Logo


**接口地址**:`/Mass_Test/portal/admin/upload/club-logo`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|clubId||query|true|integer(int64)||
|file||query|true|file||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 上传活动封面


**接口地址**:`/Mass_Test/portal/admin/upload/activity-cover`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|activityNo||query|true|string||
|file||query|true|file||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


# 角色菜单关联


## 全量分配角色菜单


**接口地址**:`/Mass_Test/sys-role-menu/assign`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>先删后增，事务；空 menuIds 表示清空绑定</p>



**请求示例**:


```javascript
{
  "roleId": 0,
  "menuIds": []
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|assignRoleMenuDTO|AssignRoleMenuDTO|body|true|AssignRoleMenuDTO|AssignRoleMenuDTO|
|&emsp;&emsp;roleId|||true|integer(int64)||
|&emsp;&emsp;menuIds|||false|array|integer(int64)|


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 查询角色已绑定菜单


**接口地址**:`/Mass_Test/sys-role-menu/listByRole/{roleId}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>返回 menu_id 列表</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|roleId||path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


# 社团申请


## 学院审批


**接口地址**:`/Mass_Test/club-application/approve/college`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>body 传 username 或 applicationNo，及 approved、opinion</p>



**请求示例**:


```javascript
{
  "approved": true,
  "opinion": "",
  "username": "",
  "applicationNo": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|clubCollegeApproveDTO|ClubCollegeApproveDTO|body|true|ClubCollegeApproveDTO|ClubCollegeApproveDTO|
|&emsp;&emsp;approved|||true|boolean||
|&emsp;&emsp;opinion|||false|string||
|&emsp;&emsp;username|||false|string||
|&emsp;&emsp;applicationNo|||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 校级审批


**接口地址**:`/Mass_Test/club-application/approve/admin`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>body 传 username（申请人）及 approved、opinion</p>



**请求示例**:


```javascript
{
  "approved": true,
  "opinion": "",
  "username": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|clubAdminApproveDTO|ClubAdminApproveDTO|body|true|ClubAdminApproveDTO|ClubAdminApproveDTO|
|&emsp;&emsp;approved|||true|boolean||
|&emsp;&emsp;opinion|||false|string||
|&emsp;&emsp;username|||true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 发起解散申请


**接口地址**:`/Mass_Test/club-application/apply/dissolve`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>社团指导老师提交解散申请</p>



**请求示例**:


```javascript
{
  "clubCode": "",
  "dissolveReason": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|clubDissolveApplyDTO|ClubDissolveApplyDTO|body|true|ClubDissolveApplyDTO|ClubDissolveApplyDTO|
|&emsp;&emsp;clubCode|||true|string||
|&emsp;&emsp;dissolveReason|||true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 发起创建申请


**接口地址**:`/Mass_Test/club-application/apply/create`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>指导老师提交社团创建申请；返回申请编号 applicationNo</p>



**请求示例**:


```javascript
{
  "clubName": "",
  "collegeId": 0,
  "category": "",
  "description": "",
  "proposedLeaderUsername": "",
  "maxMembers": 0
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|clubCreateApplyDTO|社团创建申请|body|true|ClubCreateApplyDTO|ClubCreateApplyDTO|
|&emsp;&emsp;clubName|||true|string||
|&emsp;&emsp;collegeId|||true|integer(int64)||
|&emsp;&emsp;category|社团性质，须为 ClubCategory 六类之一||true|string||
|&emsp;&emsp;description|||false|string||
|&emsp;&emsp;proposedLeaderUsername|拟定社长 username（学号/工号），非数据库 id||true|string||
|&emsp;&emsp;maxMembers|||true|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 社团性质列表


**接口地址**:`/Mass_Test/club-application/categories`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>固定六类，创建申请 category 须从中选择</p>



**请求参数**:


暂无


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 分页查询申请列表


**接口地址**:`/Mass_Test/club-application/apply/list`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>username=申请人 username；支持状态、类型、时间范围筛选</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|param||query|true|object||
|query||query|true|ClubApplication|ClubApplication|
|&emsp;&emsp;applicationNo|||false|string||
|&emsp;&emsp;applyType|||false|integer(int32)||
|&emsp;&emsp;clubName|||false|string||
|&emsp;&emsp;collegeId|||false|integer(int64)||
|&emsp;&emsp;category|||false|string||
|&emsp;&emsp;description|||false|string||
|&emsp;&emsp;proposedLeaderUsername|||false|string||
|&emsp;&emsp;maxMembers|||false|integer(int32)||
|&emsp;&emsp;dissolveReason|||false|string||
|&emsp;&emsp;applicantUsername|||false|string||
|&emsp;&emsp;applicantName|||false|string||
|&emsp;&emsp;status|||false|integer(int32)||
|&emsp;&emsp;rejectReason|||false|string||
|&emsp;&emsp;collegeApproverUsername|||false|string||
|&emsp;&emsp;collegeApproveTime|||false|string(date-time)||
|&emsp;&emsp;collegeApproveOpinion|||false|string||
|&emsp;&emsp;adminApproverUsername|||false|string||
|&emsp;&emsp;adminApproveTime|||false|string(date-time)||
|&emsp;&emsp;adminApproveOpinion|||false|string||
|&emsp;&emsp;createTime|||false|string(date-time)||
|&emsp;&emsp;updateTime|||false|string(date-time)||
|username||query|false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 申请详情


**接口地址**:`/Mass_Test/club-application/apply/detail`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>username=申请人 username</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|username||query|false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


# 社团合议


## 合议签字


**接口地址**:`/Mass_Test/club-council/council/sign/{id}`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>超管管理员签字；达成条件后自动执行解散</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id||path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 发起合议解散


**接口地址**:`/Mass_Test/club-council/council/initiate`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>超管发起；须在学院管理范围内</p>



**请求示例**:


```javascript
{
  "clubCode": "",
  "reason": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|councilInitiateDTO|CouncilInitiateDTO|body|true|CouncilInitiateDTO|CouncilInitiateDTO|
|&emsp;&emsp;clubCode|||true|string||
|&emsp;&emsp;reason|||true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


# 用户管理


## 更新用户


**接口地址**:`/Mass_Test/sys-user/updateSysUser`


**请求方式**:`PUT`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>更新自己信息</p>



**请求示例**:


```javascript
{
  "id": 0,
  "username": "",
  "password": "",
  "realName": "",
  "gender": 0,
  "phone": "",
  "email": "",
  "avatar": "",
  "userType": 0,
  "studentNo": "",
  "teacherNo": "",
  "idCard": "",
  "status": 0,
  "createTime": "",
  "updateTime": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|sysUser|SysUser|body|true|SysUser|SysUser|
|&emsp;&emsp;id|||false|integer(int64)||
|&emsp;&emsp;username|||false|string||
|&emsp;&emsp;password|||false|string||
|&emsp;&emsp;realName|||false|string||
|&emsp;&emsp;gender|||false|integer(int32)||
|&emsp;&emsp;phone|||false|string||
|&emsp;&emsp;email|||false|string||
|&emsp;&emsp;avatar|||false|string||
|&emsp;&emsp;userType|||false|integer(int32)||
|&emsp;&emsp;studentNo|||false|string||
|&emsp;&emsp;teacherNo|||false|string||
|&emsp;&emsp;idCard|||false|string||
|&emsp;&emsp;status|||false|integer(int32)||
|&emsp;&emsp;createTime|||false|string(date-time)||
|&emsp;&emsp;updateTime|||false|string(date-time)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 批量更新用户


**接口地址**:`/Mass_Test/sys-user/updateSysUserBatc`


**请求方式**:`PUT`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>管理员及以上可调用；含已禁用用户则整批拒绝</p>



**请求示例**:


```javascript
[
  {
    "id": 0,
    "username": "",
    "password": "",
    "realName": "",
    "gender": 0,
    "phone": "",
    "email": "",
    "avatar": "",
    "userType": 0,
    "studentNo": "",
    "teacherNo": "",
    "idCard": "",
    "status": 0,
    "createTime": "",
    "updateTime": ""
  }
]
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|sysUsers|SysUser|body|true|array|SysUser|
|&emsp;&emsp;id|||false|integer(int64)||
|&emsp;&emsp;username|||false|string||
|&emsp;&emsp;password|||false|string||
|&emsp;&emsp;realName|||false|string||
|&emsp;&emsp;gender|||false|integer(int32)||
|&emsp;&emsp;phone|||false|string||
|&emsp;&emsp;email|||false|string||
|&emsp;&emsp;avatar|||false|string||
|&emsp;&emsp;userType|||false|integer(int32)||
|&emsp;&emsp;studentNo|||false|string||
|&emsp;&emsp;teacherNo|||false|string||
|&emsp;&emsp;idCard|||false|string||
|&emsp;&emsp;status|||false|integer(int32)||
|&emsp;&emsp;createTime|||false|string(date-time)||
|&emsp;&emsp;updateTime|||false|string(date-time)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 批量启用-禁用用户


**接口地址**:`/Mass_Test/sys-user/toggleStatus`


**请求方式**:`PUT`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>目标须在数据范围内；禁用不可包含自己；更新后清除 Redis 缓存</p>



**请求示例**:


```javascript
{
  "usernames": [],
  "status": 0
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|toggleStatusDTO|ToggleStatusDTO|body|true|ToggleStatusDTO|ToggleStatusDTO|
|&emsp;&emsp;usernames|||false|array|string|
|&emsp;&emsp;status|||false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 新增用户


**接口地址**:`/Mass_Test/sys-user/addSysUser`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>管理员及以上可调用</p>



**请求示例**:


```javascript
{
  "id": 0,
  "username": "",
  "password": "",
  "realName": "",
  "gender": 0,
  "phone": "",
  "email": "",
  "avatar": "",
  "userType": 0,
  "studentNo": "",
  "teacherNo": "",
  "idCard": "",
  "status": 0,
  "createTime": "",
  "updateTime": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|sysUser|SysUser|body|true|SysUser|SysUser|
|&emsp;&emsp;id|||false|integer(int64)||
|&emsp;&emsp;username|||false|string||
|&emsp;&emsp;password|||false|string||
|&emsp;&emsp;realName|||false|string||
|&emsp;&emsp;gender|||false|integer(int32)||
|&emsp;&emsp;phone|||false|string||
|&emsp;&emsp;email|||false|string||
|&emsp;&emsp;avatar|||false|string||
|&emsp;&emsp;userType|||false|integer(int32)||
|&emsp;&emsp;studentNo|||false|string||
|&emsp;&emsp;teacherNo|||false|string||
|&emsp;&emsp;idCard|||false|string||
|&emsp;&emsp;status|||false|integer(int32)||
|&emsp;&emsp;createTime|||false|string(date-time)||
|&emsp;&emsp;updateTime|||false|string(date-time)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 分页查询用户


**接口地址**:`/Mass_Test/sys-user/listSysUser`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>统一查询入口，自动按权限过滤数据行并脱敏返回字段</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|param||query|true|object||
|sysUser||query|true|SysUser|SysUser|
|&emsp;&emsp;id|||false|integer(int64)||
|&emsp;&emsp;username|||false|string||
|&emsp;&emsp;password|||false|string||
|&emsp;&emsp;realName|||false|string||
|&emsp;&emsp;gender|||false|integer(int32)||
|&emsp;&emsp;phone|||false|string||
|&emsp;&emsp;email|||false|string||
|&emsp;&emsp;avatar|||false|string||
|&emsp;&emsp;userType|||false|integer(int32)||
|&emsp;&emsp;studentNo|||false|string||
|&emsp;&emsp;teacherNo|||false|string||
|&emsp;&emsp;idCard|||false|string||
|&emsp;&emsp;status|||false|integer(int32)||
|&emsp;&emsp;createTime|||false|string(date-time)||
|&emsp;&emsp;updateTime|||false|string(date-time)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 分页查询已禁用用户


**接口地址**:`/Mass_Test/sys-user/listDisabled`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>status=0，支持关键词模糊搜索，按管理员数据范围过滤</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|param||query|true|object||
|keyword||query|false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 查询用户详情


**接口地址**:`/Mass_Test/sys-user/detailSysUser/{username}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>受数据范围限制，返回字段按权限脱敏</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|username||path|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 根据username批量删除用户


**接口地址**:`/Mass_Test/sys-user/deleteSysUser`


**请求方式**:`DELETE`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>管理员及以上可调用，仅能删除权限范围内用户</p>



**请求示例**:


```javascript
[]
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|strings|string|body|true|array||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 根据username删除用户


**接口地址**:`/Mass_Test/sys-user/deleteSysUser/{username}`


**请求方式**:`DELETE`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>管理员及以上可调用</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|username||path|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


# 公开门户


## 通知列表


**接口地址**:`/Mass_Test/portal/notices`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|page||query|false|integer(int32)||
|size||query|false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 通知详情


**接口地址**:`/Mass_Test/portal/notices/{noticeNo}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|noticeNo||path|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 社团列表


**接口地址**:`/Mass_Test/portal/clubs`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|category||query|false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 活动列表


**接口地址**:`/Mass_Test/portal/activities`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|page||query|false|integer(int32)||
|size||query|false|integer(int32)||
|freezeTime||query|false|string(date-time)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 活动详情


**接口地址**:`/Mass_Test/portal/activities/{activityNo}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|activityNo||path|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


# 活动审批


## 上传总结附件


**接口地址**:`/Mass_Test/activity-apply/upload/summary`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|file||query|true|file||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 上传申请附件


**接口地址**:`/Mass_Test/activity-apply/upload/attachment`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>本地存储，返回相对路径供 submit 使用</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|file||query|true|file||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 上传活动总结


**接口地址**:`/Mass_Test/activity-apply/summary/{id}`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>活动结束后1-3天内</p>



**请求示例**:


```javascript
{
  "version": 0,
  "summaryContent": "",
  "summaryAttachment": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id||path|true|integer(int64)||
|activitySummaryDTO|ActivitySummaryDTO|body|true|ActivitySummaryDTO|ActivitySummaryDTO|
|&emsp;&emsp;version|||true|integer(int32)||
|&emsp;&emsp;summaryContent|||false|string||
|&emsp;&emsp;summaryAttachment|||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 提交活动申请


**接口地址**:`/Mass_Test/activity-apply/submit`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>部长/社长/指导老师/学院书记/管理员可发起；动态生成审批链</p>



**请求示例**:


```javascript
{
  "clubId": 0,
  "activityName": "",
  "categoryId": 0,
  "activityType": 0,
  "activityLevel": 0,
  "startTime": "",
  "endTime": "",
  "location": "",
  "locationDetail": "",
  "expectedPeople": 0,
  "budget": 0,
  "activityContent": "",
  "safetyPlan": "",
  "attachment": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|activitySubmitDTO|ActivitySubmitDTO|body|true|ActivitySubmitDTO|ActivitySubmitDTO|
|&emsp;&emsp;clubId|||true|integer(int64)||
|&emsp;&emsp;activityName|||true|string||
|&emsp;&emsp;categoryId|||true|integer(int64)||
|&emsp;&emsp;activityType|||true|integer(int32)||
|&emsp;&emsp;activityLevel|||true|integer(int32)||
|&emsp;&emsp;startTime|||true|string(date-time)||
|&emsp;&emsp;endTime|||true|string(date-time)||
|&emsp;&emsp;location|||true|string||
|&emsp;&emsp;locationDetail|||false|string||
|&emsp;&emsp;expectedPeople|||true|integer(int32)||
|&emsp;&emsp;budget|||true|number||
|&emsp;&emsp;activityContent|||true|string||
|&emsp;&emsp;safetyPlan|||true|string||
|&emsp;&emsp;attachment|||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 审批驳回


**接口地址**:`/Mass_Test/activity-apply/reject/{id}`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>必须填写驳回原因</p>



**请求示例**:


```javascript
{
  "version": 0,
  "opinion": "",
  "activityLevel": 0
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id||path|true|integer(int64)||
|activityApproveDTO|ActivityApproveDTO|body|true|ActivityApproveDTO|ActivityApproveDTO|
|&emsp;&emsp;version|||true|integer(int32)||
|&emsp;&emsp;opinion|||true|string||
|&emsp;&emsp;activityLevel|||false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 变更申请


**接口地址**:`/Mass_Test/activity-apply/change/{id}`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>已通过活动可申请变更时间/地点</p>



**请求示例**:


```javascript
{
  "version": 0,
  "startTime": "",
  "endTime": "",
  "location": "",
  "locationDetail": "",
  "changeReason": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id||path|true|integer(int64)||
|activityChangeDTO|ActivityChangeDTO|body|true|ActivityChangeDTO|ActivityChangeDTO|
|&emsp;&emsp;version|||true|integer(int32)||
|&emsp;&emsp;startTime|||true|string(date-time)||
|&emsp;&emsp;endTime|||true|string(date-time)||
|&emsp;&emsp;location|||true|string||
|&emsp;&emsp;locationDetail|||false|string||
|&emsp;&emsp;changeReason|||true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 取消活动


**接口地址**:`/Mass_Test/activity-apply/cancel/{id}`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>仅申请人可直接取消</p>



**请求示例**:


```javascript
{
  "version": 0,
  "reason": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id||path|true|integer(int64)||
|activityCancelDTO|ActivityCancelDTO|body|true|ActivityCancelDTO|ActivityCancelDTO|
|&emsp;&emsp;version|||true|integer(int32)||
|&emsp;&emsp;reason|||true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 审批通过


**接口地址**:`/Mass_Test/activity-apply/approve/{id}`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>当前步骤审批人操作；指导老师可调整活动级别</p>



**请求示例**:


```javascript
{
  "version": 0,
  "opinion": "",
  "activityLevel": 0
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id||path|true|integer(int64)||
|activityApproveDTO|ActivityApproveDTO|body|true|ActivityApproveDTO|ActivityApproveDTO|
|&emsp;&emsp;version|||true|integer(int32)||
|&emsp;&emsp;opinion|||true|string||
|&emsp;&emsp;activityLevel|||false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 分页查询


**接口地址**:`/Mass_Test/activity-apply/list`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>支持条件筛选与排序</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|param||query|true|object||
|query||query|true|ActivityApply|ActivityApply|
|&emsp;&emsp;id|||false|integer(int64)||
|&emsp;&emsp;activityNo|||false|string||
|&emsp;&emsp;clubId|||false|integer(int64)||
|&emsp;&emsp;activityName|||false|string||
|&emsp;&emsp;categoryId|||false|integer(int64)||
|&emsp;&emsp;activityType|||false|integer(int32)||
|&emsp;&emsp;startTime|||false|string(date-time)||
|&emsp;&emsp;endTime|||false|string(date-time)||
|&emsp;&emsp;location|||false|string||
|&emsp;&emsp;locationDetail|||false|string||
|&emsp;&emsp;expectedPeople|||false|integer(int32)||
|&emsp;&emsp;budget|||false|number||
|&emsp;&emsp;activityContent|||false|string||
|&emsp;&emsp;coverImage|||false|string||
|&emsp;&emsp;organizerNote|||false|string||
|&emsp;&emsp;safetyPlan|||false|string||
|&emsp;&emsp;attachment|||false|string||
|&emsp;&emsp;applyUsername|||false|string||
|&emsp;&emsp;applyTime|||false|string(date-time)||
|&emsp;&emsp;currentApproveStep|||false|integer(int32)||
|&emsp;&emsp;approveStatus|||false|integer(int32)||
|&emsp;&emsp;activityLevel|||false|integer(int32)||
|&emsp;&emsp;levelAdjustLocked|||false|integer(int32)||
|&emsp;&emsp;version|||false|integer(int32)||
|&emsp;&emsp;rejectReason|||false|string||
|&emsp;&emsp;summaryContent|||false|string||
|&emsp;&emsp;summaryAttachment|||false|string||
|&emsp;&emsp;summaryUploadTime|||false|string(date-time)||
|&emsp;&emsp;createTime|||false|string(date-time)||
|&emsp;&emsp;updateTime|||false|string(date-time)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 活动详情


**接口地址**:`/Mass_Test/activity-apply/detail/{id}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>含审批流与变更历史</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id||path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


# 菜单管理


## 新增或更新菜单


**接口地址**:`/Mass_Test/sys-menu/save`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>校验父级、循环引用、同级名称唯一、类型字段；清除菜单缓存</p>



**请求示例**:


```javascript
{
  "id": 0,
  "parentId": 0,
  "menuName": "",
  "menuType": 0,
  "permissionCode": "",
  "componentPath": "",
  "routePath": "",
  "icon": "",
  "sort": 0,
  "status": 0,
  "createTime": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|sysMenu|SysMenu|body|true|SysMenu|SysMenu|
|&emsp;&emsp;id|||false|integer(int64)||
|&emsp;&emsp;parentId|||false|integer(int64)||
|&emsp;&emsp;menuName|||true|string||
|&emsp;&emsp;menuType|||true|integer(int32)||
|&emsp;&emsp;permissionCode|||false|string||
|&emsp;&emsp;componentPath|||false|string||
|&emsp;&emsp;routePath|||false|string||
|&emsp;&emsp;icon|||false|string||
|&emsp;&emsp;sort|||false|integer(int32)||
|&emsp;&emsp;status|||false|integer(int32)||
|&emsp;&emsp;createTime|||false|string(date-time)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 当前用户可见菜单树


**接口地址**:`/Mass_Test/sys-menu/tree`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>返回菜单树 + 按钮权限集合；按 userId 缓存</p>



**请求参数**:


暂无


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 分页查询菜单


**接口地址**:`/Mass_Test/sys-menu/list`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>支持 menuName 模糊、menuType 过滤，平铺返回</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|param||query|true|object||
|sysMenu||query|true|SysMenu|SysMenu|
|&emsp;&emsp;id|||false|integer(int64)||
|&emsp;&emsp;parentId|||false|integer(int64)||
|&emsp;&emsp;menuName|||true|string||
|&emsp;&emsp;menuType|||true|integer(int32)||
|&emsp;&emsp;permissionCode|||false|string||
|&emsp;&emsp;componentPath|||false|string||
|&emsp;&emsp;routePath|||false|string||
|&emsp;&emsp;icon|||false|string||
|&emsp;&emsp;sort|||false|integer(int32)||
|&emsp;&emsp;status|||false|integer(int32)||
|&emsp;&emsp;createTime|||false|string(date-time)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 删除菜单


**接口地址**:`/Mass_Test/sys-menu/delete/{id}`


**请求方式**:`DELETE`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>无子菜单且未被更高权限角色绑定时删除，并清理 sys_role_menu</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id||path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


# 登录认证


## 退出登录


**接口地址**:`/Mass_Test/login/logout`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>删除当前用户 Redis 会话缓存；需携带有效 Token</p>



**请求参数**:


暂无


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 用户登录


**接口地址**:`/Mass_Test/login/allocation`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>使用学号/工号和密码进行身份认证，成功返回JWT令牌和用户名</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|name||query|true|string||
|password||query|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


# 用户注册


## 用户注册


**接口地址**:`/Mass_Test/register/single`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>使用注册信息（学号/工号、密码、姓名等）创建新用户，无需登录</p>



**请求示例**:


```javascript
{
  "username": "",
  "realName": "",
  "password": "",
  "gender": 0,
  "userType": 0
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|registerDTO|RegisterDTO|body|true|RegisterDTO|RegisterDTO|
|&emsp;&emsp;username|||false|string||
|&emsp;&emsp;realName|||true|string||
|&emsp;&emsp;password|||true|string||
|&emsp;&emsp;gender|||false|integer(int32)||
|&emsp;&emsp;userType|||false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


# 用户角色关联


## 分配角色


**接口地址**:`/Mass_Test/sys-user-role/assign`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>目标用户须在范围内且未禁用；校验 scope 与防重复</p>



**请求示例**:


```javascript
{
  "username": "",
  "roleId": 0,
  "scopeType": 0,
  "scopeId": 0
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|assignRoleDTO|AssignRoleDTO|body|true|AssignRoleDTO|AssignRoleDTO|
|&emsp;&emsp;username|||true|string||
|&emsp;&emsp;roleId|||true|integer(int64)||
|&emsp;&emsp;scopeType|||false|integer(int32)||
|&emsp;&emsp;scopeId|||false|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 按用户名查询角色


**接口地址**:`/Mass_Test/sys-user-role/roles/{username}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>目标用户须在数据范围内</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|username||path|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 查询自己的角色


**接口地址**:`/Mass_Test/sys-user-role/my-roles`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>仅返回当前登录用户的角色列表</p>



**请求参数**:


暂无


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 分页查询用户角色列表


**接口地址**:`/Mass_Test/sys-user-role/list`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>联查用户姓名、角色名称，按管理员数据范围过滤</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|param||query|true|object||
|keyword||query|false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```


## 撤销角色


**接口地址**:`/Mass_Test/sys-user-role/revoke/{id}`


**请求方式**:`DELETE`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>不能撤销高于自身等级的角色，不能撤销自己持有的角色</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id||path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|R|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|empty||boolean||


**响应示例**:
```javascript
{
	"empty": true
}
```