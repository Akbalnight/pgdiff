function requestAPI(url, data) {
    return new Promise((resolve, reject) => {
        $.ajax({
            url: url,
            type: "POST",
            headers: { "Accept" : "application/json; charset=utf-8", "Content-Type": "application/json; charset=utf-8"},
            data: JSON.stringify(data),
            error: function(error) { reject(error.responseJSON); },
            success: function(response){ resolve(response); }
        });
    })
}

function getOneConnect() {
    return {
        host: $('#host1').val(),
        post: $('#port1').val(),
        dbname: $('#dbname1').val(),
        schema: $('#schema1').val(),
        username: $('#username1').val(),
        password: $('#password1').val(),
    }
}

function getTwoConnect() {
    return {
        host: $('#host2').val(),
        post: $('#port2').val(),
        dbname: $('#dbname2').val(),
        schema: $('#schema2').val(),
        username: $('#username2').val(),
        password: $('#password2').val(),
    }
}

function getConnectSpinner(){
    return '' +
        '<div class="d-flex justify-content-center">' +
        '   <div class="spinner-border spinner-border-sm text-info" role="status"> ' +
        '       <span class="sr-only align-middle">Loading...</span> ' +
        '   </div>' +
        '</div>'
}
