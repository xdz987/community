$(function (){
    $("#uploadForm").submit(upload);
});

function upload(){
    $.ajax({
        url:"http://upload-z2.qiniup.com",
        method:"post",
        // 拒绝将表单内容转为字符串
        processData:false,
        // 不让jQuery设置上传的类型，让浏览器自己设置。
        // 因为文件是二进阶的，和其他数据混在一起时存在边界需处理，浏览器会设定边界，而jQuery没有设置
        contentType:false,
        // JS对象，用来存放表单数据
        // jQuery第0个值得到一个DOM对象
        data:new FormData($("#uploadForm")[0]),
        success:function (data){
            // 七牛云返回的data是json
            if(data && data.code == 0){
                // 更新头像访问路径
                $.post(
                    CONTEXT_PATH + "/user/header/url",
                    {"fileName":$("input[name='key']").val()},
                    // 自己服务器返回的是字符串
                    function (data){
                        data = $.parseJSON(data);
                        if(data.code==0){
                            window.location.reload();
                        }else {
                            alert(data.msg);
                        }
                    }
                )
            }
            else{
                alert("上传失败！");
            }
        }
    });

    // 阻止继续submit
    return false;
}