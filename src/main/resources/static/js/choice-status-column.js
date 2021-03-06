//Список статусов возможных для клиентов-студентов
$(function () {
    $('#choice-status-column-modal').on('show.bs.modal', function () {
        $("#status-column").empty();
        $("#new-client-status").empty();
        $.ajax({
            type: 'POST',
            url: '/slack/get/students/statuses',
            dataType: 'json',
            success: function (json) {
                $.each(json, function (index, element) {
                    $('#status-column')
                        .append($('<option value=' + element.id + '>')
                            .append(element.name)
                            .append('</option>'));
                });
                $.ajax({
                    type: 'GET',
                    url: '/rest/properties',
                    dataType: 'json',
                    success: function (response) {
                        $('#status-column').val(response.defaultStatusId);
                    }
                });
            }
        });

        $.ajax({
            type: 'GET',
            url: '/rest/status',
            dataType: 'json',
            success: function (response) {
                $.each(response, function (index, element) {
                    $("#new-client-status").append("<option id = default_status_" + element.id + " value=" + element.id + ">" + element.name + "</option>");
                });

                $.ajax({
                    type: 'GET',
                    url: '/rest/properties',
                    dataType: 'json',
                    success: function (response) {
                        $("#new-client-status").val(response.newClientStatus);
                    }
                });
            }
        });
    });
});

//Выбираем и сохраняем дефолтный статус
$('#update-status').click(function () {
    let selectedId = $("select#status-column").val();
    $.ajax({
        type: 'GET',
        url: '/slack/set/default/' + selectedId,
    });

    let new_client_status = $("#new-client-status").val();
    $.ajax({
        type: 'POST',
        url: '/rest/properties/new-user-status',
        data: {statusId : new_client_status}
    });
});
