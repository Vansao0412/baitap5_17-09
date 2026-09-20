let categories = [];
let products = [];

function showMessage(message, success = true) {
    $('#message').html('<div class="alert alert-' + (success ? 'success' : 'danger') + '">' + message + '</div>');
    setTimeout(function () { $('#message').empty(); }, 3000);
}

function escapeHtml(value) {
    return $('<div>').text(value == null ? '' : value).html();
}

function imageHtml(filename) {
    return filename ? '<img class="preview" src="/uploads/' + encodeURIComponent(filename) + '" alt="Ảnh">' : '<span class="text-muted">Không có</span>';
}

function apiError(xhr) {
    let response = xhr.responseJSON;
    return response && response.message ? response.message : 'Có lỗi xảy ra khi gọi API';
}

function loadCategories() {
    $.getJSON('/api/category', function (response) {
        categories = response.body || [];
        let rows = '';
        categories.forEach(function (category) {
            rows += '<tr>' +
                '<td>' + category.categoryId + '</td>' +
                '<td>' + imageHtml(category.icon) + '</td>' +
                '<td>' + escapeHtml(category.categoryName) + '</td>' +
                '<td class="text-end">' +
                '<button class="btn btn-sm btn-outline-warning category-edit" data-id="' + category.categoryId + '">Sửa</button> ' +
                '<button class="btn btn-sm btn-outline-danger category-delete" data-id="' + category.categoryId + '">Xóa</button>' +
                '</td></tr>';
        });
        $('#category-table tbody').html(rows || '<tr><td colspan="4" class="text-center">Chưa có dữ liệu</td></tr>');
        let options = '<option value="">-- Chọn category --</option>';
        categories.forEach(function (category) {
            options += '<option value="' + category.categoryId + '">' + escapeHtml(category.categoryName) + '</option>';
        });
        $('#product-category').html(options);
    }).fail(function (xhr) { showMessage(apiError(xhr), false); });
}

function loadProducts() {
    $.getJSON('/api/product', function (response) {
        products = response.body || [];
        let rows = '';
        products.forEach(function (product) {
            let categoryName = product.category ? product.category.categoryName : '';
            rows += '<tr>' +
                '<td>' + product.productId + '</td>' +
                '<td>' + imageHtml(product.images) + '</td>' +
                '<td>' + escapeHtml(product.productName) + '</td>' +
                '<td>' + escapeHtml(categoryName) + '</td>' +
                '<td>' + Number(product.unitPrice).toLocaleString('vi-VN') + '</td>' +
                '<td>' + product.quantity + '</td>' +
                '<td>' + (product.status === 1 ? 'Đang bán' : 'Ngừng bán') + '</td>' +
                '<td class="text-end">' +
                '<button class="btn btn-sm btn-outline-warning product-edit" data-id="' + product.productId + '">Sửa</button> ' +
                '<button class="btn btn-sm btn-outline-danger product-delete" data-id="' + product.productId + '">Xóa</button>' +
                '</td></tr>';
        });
        $('#product-table tbody').html(rows || '<tr><td colspan="8" class="text-center">Chưa có dữ liệu</td></tr>');
    }).fail(function (xhr) { showMessage(apiError(xhr), false); });
}

function resetCategoryForm() {
    $('#category-id').val('');
    $('#category-form')[0].reset();
}

function resetProductForm() {
    $('#product-id').val('');
    $('#product-form')[0].reset();
    $('#product-discount').val('0');
    $('#product-status').val('1');
}

$(function () {
    loadCategories();
    loadProducts();

    $('#category-form').submit(function (event) {
        event.preventDefault();
        let id = $('#category-id').val();
        let formData = new FormData(this);
        let url = id ? '/api/category/' + id : '/api/category';
        $.ajax({ url: url, type: id ? 'PUT' : 'POST', data: formData, dataType: 'json', contentType: false, processData: false })
            .done(function (response) { showMessage(response.message); resetCategoryForm(); loadCategories(); loadProducts(); })
            .fail(function (xhr) { showMessage(apiError(xhr), false); });
    });

    $('#product-form').submit(function (event) {
        event.preventDefault();
        let id = $('#product-id').val();
        let formData = new FormData(this);
        let url = id ? '/api/product/' + id : '/api/product';
        $.ajax({ url: url, type: id ? 'PUT' : 'POST', data: formData, dataType: 'json', contentType: false, processData: false })
            .done(function (response) { showMessage(response.message); resetProductForm(); loadProducts(); })
            .fail(function (xhr) { showMessage(apiError(xhr), false); });
    });

    $('#category-reset, #category-cancel').click(resetCategoryForm);
    $('#product-reset, #product-cancel').click(resetProductForm);

    $(document).on('click', '.category-edit', function () {
        let item = categories.find(function (category) { return category.categoryId == $(this).data('id'); }.bind(this));
        if (!item) return;
        $('#category-id').val(item.categoryId);
        $('#category-name').val(item.categoryName);
        window.scrollTo({ top: 0, behavior: 'smooth' });
    });

    $(document).on('click', '.category-delete', function () {
        let id = $(this).data('id');
        if (!confirm('Bạn có chắc muốn xóa category này?')) return;
        $.ajax({ url: '/api/category/' + id, type: 'DELETE', dataType: 'json' })
            .done(function (response) { showMessage(response.message); loadCategories(); loadProducts(); })
            .fail(function (xhr) { showMessage(apiError(xhr), false); });
    });

    $(document).on('click', '.product-edit', function () {
        let item = products.find(function (product) { return product.productId == $(this).data('id'); }.bind(this));
        if (!item) return;
        $('#product-id').val(item.productId);
        $('#product-name').val(item.productName);
        $('#product-category').val(item.category ? item.category.categoryId : '');
        $('#product-price').val(item.unitPrice);
        $('#product-discount').val(item.discount);
        $('#product-quantity').val(item.quantity);
        $('#product-status').val(item.status);
        $('#product-description').val(item.description);
        document.getElementById('product-form').scrollIntoView({ behavior: 'smooth' });
    });

    $(document).on('click', '.product-delete', function () {
        let id = $(this).data('id');
        if (!confirm('Bạn có chắc muốn xóa product này?')) return;
        $.ajax({ url: '/api/product/' + id, type: 'DELETE', dataType: 'json' })
            .done(function (response) { showMessage(response.message); loadProducts(); })
            .fail(function (xhr) { showMessage(apiError(xhr), false); });
    });
});
