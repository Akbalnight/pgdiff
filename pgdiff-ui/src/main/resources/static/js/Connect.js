$('#ConnOne').click(function () {
    $('#resultConnect1').html(getConnectSpinner());
    requestAPI("/test-connect", getOneConnect())
        .then(response => { $('#resultConnect1').html(response.msg).css({color: "green"}) })
        .catch(error => { $('#resultConnect1').html(error.msg).css({color: "red"}) });
});

$('#ConnTwo').click(function () {
    $('#resultConnect2').html(getConnectSpinner());
    requestAPI("/test-connect", getTwoConnect())
        .then(response => { $('#resultConnect2').html(response.msg).css({color: "green"}) })
        .catch(error => { $('#resultConnect2').html(error.msg).css({color: "red"}) });
});
