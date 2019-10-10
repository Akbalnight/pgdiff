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
            // $('#tableDiff').bootstrapTable('hideLoading');
            // $('#tableDiff').bootstrapTable('load', response);
            // $('#tableDiff').bootstrapTable('filterBy', { resultCode: [0, 1, 4] })
            // $('#tableDiff').bootstrapTable('expandAllRows');
            var sourceSql = [];
            var destSql = [];

            for(var i = 0; i <  response.length; i++){
                var row = response[i];

                if(row.resultCode !== -1) {

                    var ddl = row.resultCode === 0 ? "\n" + row.ddlTableOne : '';
                    var alters = row.alters ? "\n" + row.alters.join('\n') : '';


                    var ddlOneLen = row.ddlTableOne ? row.ddlTableOne.split('\n').length : 0;
                    var ddlTwoLen = row.ddlTableTwo ? row.ddlTableTwo.split('\n').length : 0;

                    var ddlOneOffset = 0;
                    var ddlTwoOffset = 0;

                    if(ddlOneLen > ddlTwoLen) ddlTwoOffset = ddlOneLen - ddlTwoLen;
                    else ddlOneOffset = ddlTwoLen - ddlOneLen;

                    sourceSql.push("-- Source: " + row.nameTableOne + "" +
                        "\n-- Destination: " + row.nameTableTwo +
                        "\n" + getDestDdl(row.ddlTableOne, 0, ddlOneOffset) +
                        alters + "\n\n");


                    destSql.push(getDestDdl( row.ddlTableTwo ? row.ddlTableTwo : "", 2, row.alters ? row.alters.length + ddlTwoOffset + 2 : ddlTwoOffset + 2));
                }
            }
            source.setValue(sourceSql.join(''));
            dest.setValue(destSql.join(''));




            // w3CodeColor();

            // editor.setValue(selectSql.join('')); // задаем
        })
        .catch(error => {
            $('#resultDiff').html(error.msg).css({color: "red"})

            $('#tableDiff').bootstrapTable('hideLoading')
            console.log(error);
        });

})

function getDestDdl(ddl, countPrefixLine, countSuffixLine){
    // console.log("ddl: ", ddl);
    // console.log("countPrefixLine: ", countPrefixLine);
    // console.log("countSuffixLine: ", countSuffixLine);

    var resDDL = "";

    for(var i = 0; i < countPrefixLine; i++) resDDL += "\n";

    resDDL += ddl !== "" ? ddl + "\n" : "";

    for(var i = 0; i < countSuffixLine; i++) resDDL += "\n";

    return resDDL;
}

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
            var columns = item.alters ? "\n" + item.alters.join('\n') : '';

            selectSql.push("-- Source: " + item.nameTableOne + " \n-- Destination: " + item.nameTableTwo + ddl + columns + "\n\n");
        }
    });
    return selectSql;
}

