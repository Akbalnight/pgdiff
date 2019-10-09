$('#findDiff').click(function () {

    // $('#resultDiff').html(getConnectSpinner());
    $('#tableDiff').bootstrapTable('showLoading')

    var data_req = {
        databaseSettingsOne: getOneConnect(),
        databaseSettingsTwo: getTwoConnect(),
        operations:[$('#operation').val()],
        withPartitions: $('#withPartitions').is(':checked')
    };


    requestAPI("/find-diff", data_req)
        .then(response => {
            console.log(response);
            $('#tableDiff').bootstrapTable('hideLoading');
            $('#tableDiff').bootstrapTable('load', response);
            $('#tableDiff').bootstrapTable('filterBy', { resultCode: [0, 1, 4] })
            $('#tableDiff').bootstrapTable('expandAllRows');
            w3CodeColor();
        })
        .catch(error => {
            $('#resultDiff').html(error.msg).css({color: "red"})

            $('#tableDiff').bootstrapTable('hideLoading')
            console.log(error);
        });

})

$('#copyDiff').click(function () {
    // var selectRows = $('#tableDiff').bootstrapTable('getSelections')
    // var selectSql = "";
    // selectRows.forEach(function(item, i) {
    //     selectSql += "-- Source: " + item.source +" \n-- Destination: " + item.destination + "\n" + item.action+"\n\n";
    // });

    var selectRows = getSelectRowsSql()

    navigator.clipboard.writeText(selectRows.join(''))
        .then(() => {
            // console.log('Copied');
            $('#resultCopy').html(`Copied ${selectRows.length} rows to clipboard`).css({color: "green"})
            $('.toast').toast('show');
        })
        .catch(err => {
            console.log('Something went wrong', err);
            $('#resultCopy').html(error.msg).css({color: "red"})
            $('.toast').toast('show');
        })
});

$('#exportDiff').click(function () {
    var blob = new Blob(getSelectRowsSql(), {type: "application/sql;charset=utf-8"});
    module$FileSaver.saveAs(blob, "export.sql");
});

function getSelectRowsSql() {
    var selectRows = $('#tableDiff').bootstrapTable('getSelections')
    var selectSql = [];
    selectRows.forEach(function(item, i) {
        // console.log(item);
        // selectSql.push("-- Source: " + item.source +" \n-- Destination: " + item.destination + "\n" + item.action+"\n\n");

        if(item.resultCode !== -1) {
            var ddl = item.resultCode === 0 ? "\n" + item.ddlTableOne : '';
            var columns = item.columnAlters ? "\n" + item.columnAlters.join('\n') : '';

            selectSql.push("-- Source: " + item.nameTableOne + " \n-- Destination: " + item.nameTableTwo + ddl + columns + "\n\n");
        }
    });
    return selectSql;
}

