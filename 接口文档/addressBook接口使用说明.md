[/user/address/add   POST
新增地址，需要json参数：

private String consignee;
private String phone;
private String sex; //性别 0女 1男
private String detail;  //详细地址
private String label;   //标签(可选)


/user/address/delete?id={addressBookId}     GET 
删除一个地址

/user/address/get?id={addressBookId}      GET
获取一个地址

/user/address/getall    GET
获取当前用户的所有地址