$(function () {
    $("#publishBtn").click(publish);
});

function publish() {
    $("#publishModal").modal("hide");

    // 发送AJAX请求之前，将CSRF令牌设置到请求的消息头中
    // var token = $("meta[name='_csrf']").attr("content");
    // var header = $("meta[name='_csrf_header']").attr("content");
    // $(document).ajaxSend(function (e,xhr,options){
    //     xhr.setRequestHeader(header,token);
    // });

    // 获取标题和内容
    var title = $("#recipient-name").val();
    var content = $("#message-text").val();

    // 发送异步请求
    $.ajax({
        type: 'post',
        url: CONTEXT_PATH + "/discuss/add",
        contentType: "application/x-www-form-urlencoded",
        data: {
            title:title,
            content:content
        },
        dataType:'json',
        success: function(data) {
            $("#hintBody").text(data.msg);

            // 显示提示框
            $("#hintModal").modal("show");
            setTimeout(function () {
                $("#hintModal").modal("hide");
                // 刷新页面
                if (data.code == 0) {
                    window.location.reload();
                }
            }, 2000);
        }
    });
}