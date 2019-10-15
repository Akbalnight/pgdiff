$('#findDiff').click(function () {

    // $('#resultDiff').html(getConnectSpinner());
    // $('#tableDiff').bootstrapTable('showLoading')

    var data_req = {
        databaseSettingsOne: getOneConnect(),
        databaseSettingsTwo: getTwoConnect(),
        operations:[$('#operation').val()],
        withPartitions: $('#withPartitions').is(':checked'),
        withTableDDL: $('#withDDL').is(':checked')
    };


    requestAPI("/find-diff", data_req)
        .then(response => {

            var sourceSql = [];
            var destSql = [];

            sourceSql.push("--- --- --- Pg Diff --- --- ---");
            destSql.push("--- --- --- --- --- ---");
            // sourceSql.push("--- --- --- This diff between {0}:{1}/{2} and --- --- --- >".format([$('#host1').val(), $('#port1').val(), $('#dbname1').val()]));
            // destSql.push("< --- --- --- {0}:{1}/{2} --- --- ---".format([$('#host2').val(), $('#port2').val(), $('#dbname2').val()]));

            var lastObjectName = "";

            for(var i = 0; i <  response.length; i++){
                var row = response[i];
                var headGroup = "";
                var headGroupLen = 0;

                if(lastObjectName === row.objectName) {
                    headGroup = "";
                    headGroupLen = 0;
                }
                else {
                    lastObjectName = row.objectName;
                    headGroup = "\n\n--- Object name: " + lastObjectName + "\n";
                    headGroupLen = 3;
                }

                var alter    = row.alter ? row.alter : "";
                var subAlter = row.subAlter ? row.subAlter : "";

                var alterLen    = alter !== "" ? alter.split('\n').length : 0;
                var subAlterLen = subAlter !== "" ? subAlter.split('\n').length : 0;

                var alterOffset = 0;
                var subAlterOffset = 0;

                if(alterLen > subAlterLen)
                    subAlterOffset = alterLen - subAlterLen;
                else
                    alterOffset = subAlterLen - alterLen;


                sourceSql.push(headGroup + getAlterWithOffset(alter, 0, alterOffset));
                destSql.push(getAlterWithOffset(subAlter, headGroupLen, subAlterOffset));

            }
            source.session.setValue(sourceSql.join(""));
            dest.session.setValue(destSql.join(""));

        })
        .catch(error => {
            // $('#resultDiff').html(error.msg).css({color: "red"})
            // $('#tableDiff').bootstrapTable('hideLoading')
            console.log(error);
        });

})

function getAlterWithOffset(ddl, countPrefixLine, countSuffixLine){

    var resDDL = "";

    for(var i = 0; i < countPrefixLine; i++) resDDL += "-\n";

    resDDL += ddl !== "" ? ddl + "\n" : "";

    for(var i = 0; i < countSuffixLine; i++) resDDL += "-\n";

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
    // console.log(source.session.getValue());
    var blob = new Blob([source.session.getValue()], {type: "application/sql;charset=utf-8"});
    module$FileSaver.saveAs(blob, "export.sql");
});

function getSelectRowsSql() {
    // var selectRows = $('#tableDiff').bootstrapTable('getSelections')
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

