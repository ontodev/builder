function createTask(name) {
    $.post(name + "/run", function() {
                location.reload();
            }
    );
}

function cancelTask(id) {
    $.ajax({
        url: id,
        type: 'DELETE',
        success: function() { location.reload(); }
    });
}