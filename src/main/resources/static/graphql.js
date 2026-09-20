let gqlCategories = [];
let categoryPage = 0;
let productPage = 0;
let categoryTotalPages = 0;
let productTotalPages = 0;

const categoryFields = 'categoryId categoryName icon';
const productFields = 'productId productName quantity unitPrice images description discount status category { categoryId categoryName }';

function gqlRequest(query, variables, done) {
    $.ajax({
        url: '/graphql', type: 'POST', contentType: 'application/json',
        data: JSON.stringify({ query: query, variables: variables || {} })
    }).done(function (response) {
        if (response.errors && response.errors.length) {
            showGqlMessage(response.errors[0].message, false);
            return;
        }
        done(response.data || {});
    }).fail(function () { showGqlMessage('Không gọi được GraphQL API', false); });
}

function showGqlMessage(message, success) {
    $('#graphql-message').html('<div class="alert alert-' + (success === false ? 'danger' : 'success') + '">' + escapeGql(message) + '</div>');
    setTimeout(function () { $('#graphql-message').empty(); }, 3000);
}

function escapeGql(value) { return $('<div>').text(value == null ? '' : value).html(); }

function gqlImage(filename) {
    return filename ? '<img class="preview" src="/uploads/' + encodeURIComponent(filename) + '" alt="Ảnh">' : '<span class="text-muted">Không có</span>';
}

function loadGqlCategoriesForSelect() {
    gqlRequest('{ categories { ' + categoryFields + ' } }', {}, function (data) {
        gqlCategories = data.categories || [];
        let options = '<option value="">-- Chọn category --</option>';
        let homeOptions = '<option value="">-- Tất cả category --</option>';
        gqlCategories.forEach(function (item) {
            options += '<option value="' + item.categoryId + '">' + escapeGql(item.categoryName) + '</option>';
            homeOptions += '<option value="' + item.categoryId + '">' + escapeGql(item.categoryName) + '</option>';
        });
        $('#g-product-category').html(options);
        $('#home-category').html(homeOptions);
    });
}

function renderHomeProducts(items) {
    let rows = '';
    (items || []).forEach(function (item) {
        rows += '<tr><td>' + item.productId + '</td><td>' + gqlImage(item.images) + '</td><td>' + escapeGql(item.productName) + '</td>' +
            '<td>' + escapeGql(item.category ? item.category.categoryName : '') + '</td><td>' + Number(item.unitPrice).toLocaleString('vi-VN') +
            '</td><td>' + item.quantity + '</td><td>' + (item.status === 1 ? 'Đang bán' : 'Ngừng bán') + '</td></tr>';
    });
    $('#home-product-table tbody').html(rows || '<tr><td colspan="7" class="text-center">Chưa có dữ liệu</td></tr>');
}

function loadHomeProducts() {
    let categoryId = $('#home-category').val();
    let query = categoryId ? '{ productsByCategory(categoryId: "' + categoryId + '") { ' + productFields + ' } }' : '{ products { ' + productFields + ' } }';
    gqlRequest(query, {}, function (data) { renderHomeProducts(categoryId ? data.productsByCategory : data.products); });
}

function loadCategoryPage() {
    gqlRequest('query($page:Int!, $size:Int!, $keyword:String) { categoriesPage(page:$page, size:$size, keyword:$keyword) { content { ' + categoryFields + ' } page size totalElements totalPages } }',
        { page: categoryPage, size: 5, keyword: $('#g-category-search').val() || null }, function (data) {
            let page = data.categoriesPage;
            categoryTotalPages = page.totalPages;
            let rows = '';
            (page.content || []).forEach(function (item) {
                rows += '<tr><td>' + item.categoryId + '</td><td>' + gqlImage(item.icon) + '</td><td>' + escapeGql(item.categoryName) + '</td><td class="text-end">' +
                    '<button class="btn btn-sm btn-outline-warning g-category-edit" data-id="' + item.categoryId + '">Sửa</button> ' +
                    '<button class="btn btn-sm btn-outline-danger g-category-delete" data-id="' + item.categoryId + '">Xóa</button></td></tr>';
            });
            $('#g-category-table tbody').html(rows || '<tr><td colspan="4" class="text-center">Chưa có dữ liệu</td></tr>');
            $('#g-category-info').text('Trang ' + (page.page + 1) + '/' + Math.max(page.totalPages, 1) + ' - ' + page.totalElements + ' category');
        });
}

function loadProductPage() {
    gqlRequest('query($page:Int!, $size:Int!, $keyword:String) { productsPage(page:$page, size:$size, keyword:$keyword) { content { ' + productFields + ' } page size totalElements totalPages } }',
        { page: productPage, size: 5, keyword: $('#g-product-search').val() || null }, function (data) {
            let page = data.productsPage;
            productTotalPages = page.totalPages;
            let rows = '';
            (page.content || []).forEach(function (item) {
                rows += '<tr><td>' + item.productId + '</td><td>' + gqlImage(item.images) + '</td><td>' + escapeGql(item.productName) + '</td>' +
                    '<td>' + escapeGql(item.category ? item.category.categoryName : '') + '</td><td>' + Number(item.unitPrice).toLocaleString('vi-VN') + '</td><td>' + item.quantity + '</td><td class="text-end">' +
                    '<button class="btn btn-sm btn-outline-warning g-product-edit" data-item=\'' + JSON.stringify(item).replace(/'/g, '&#39;') + '\'>Sửa</button> ' +
                    '<button class="btn btn-sm btn-outline-danger g-product-delete" data-id="' + item.productId + '">Xóa</button></td></tr>';
            });
            $('#g-product-table tbody').html(rows || '<tr><td colspan="7" class="text-center">Chưa có dữ liệu</td></tr>');
            $('#g-product-info').text('Trang ' + (page.page + 1) + '/' + Math.max(page.totalPages, 1) + ' - ' + page.totalElements + ' product');
        });
}

function resetGqlCategory() { $('#g-category-id').val(''); $('#g-category-form')[0].reset(); }
function resetGqlProduct() { $('#g-product-id').val(''); $('#g-product-form')[0].reset(); $('#g-product-discount').val('0'); $('#g-product-status').val('1'); }

$(function () {
    loadGqlCategoriesForSelect();
    loadHomeProducts();
    loadCategoryPage();
    loadProductPage();

    $('#home-category').change(loadHomeProducts);
    $('#g-category-search-btn').click(function () { categoryPage = 0; loadCategoryPage(); });
    $('#g-product-search-btn').click(function () { productPage = 0; loadProductPage(); });
    $('#g-category-prev').click(function () { if (categoryPage > 0) { categoryPage--; loadCategoryPage(); } });
    $('#g-category-next').click(function () { if (categoryPage + 1 < categoryTotalPages) { categoryPage++; loadCategoryPage(); } });
    $('#g-product-prev').click(function () { if (productPage > 0) { productPage--; loadProductPage(); } });
    $('#g-product-next').click(function () { if (productPage + 1 < productTotalPages) { productPage++; loadProductPage(); } });
    $('#g-category-reset').click(resetGqlCategory);
    $('#g-product-reset').click(resetGqlProduct);

    $('#g-category-form').submit(function (event) {
        event.preventDefault();
        let id = $('#g-category-id').val();
        let input = { categoryName: $('#g-category-name').val(), icon: $('#g-category-icon').val() || null };
        let mutation = id ? 'mutation($id:ID!, $input:CategoryInput!) { updateCategory(id:$id, input:$input) { categoryId } }' : 'mutation($input:CategoryInput!) { createCategory(input:$input) { categoryId } }';
        gqlRequest(mutation, id ? { id: String(id), input: input } : { input: input }, function () { showGqlMessage('Lưu category thành công'); resetGqlCategory(); loadGqlCategoriesForSelect(); loadCategoryPage(); loadHomeProducts(); });
    });

    $('#g-product-form').submit(function (event) {
        event.preventDefault();
        let id = $('#g-product-id').val();
        let input = { productName: $('#g-product-name').val(), unitPrice: Number($('#g-product-price').val()), discount: Number($('#g-product-discount').val() || 0), quantity: Number($('#g-product-quantity').val()), status: Number($('#g-product-status').val()), description: $('#g-product-description').val(), images: $('#g-product-images').val() || null, categoryId: String($('#g-product-category').val()) };
        let mutation = id ? 'mutation($id:ID!, $input:ProductInput!) { updateProduct(id:$id, input:$input) { productId } }' : 'mutation($input:ProductInput!) { createProduct(input:$input) { productId } }';
        gqlRequest(mutation, id ? { id: String(id), input: input } : { input: input }, function () { showGqlMessage('Lưu product thành công'); resetGqlProduct(); loadProductPage(); loadHomeProducts(); });
    });

    $(document).on('click', '.g-category-edit', function () {
        let item = gqlCategories.find(function (value) { return String(value.categoryId) === String($(this).data('id')); }.bind(this));
        if (!item) return;
        $('#g-category-id').val(item.categoryId); $('#g-category-name').val(item.categoryName); $('#g-category-icon').val(item.icon || '');
        window.scrollTo({ top: 450, behavior: 'smooth' });
    });

    $(document).on('click', '.g-category-delete', function () {
        if (!confirm('Bạn có chắc muốn xóa category này?')) return;
        gqlRequest('mutation($id:ID!) { deleteCategory(id:$id) }', { id: String($(this).data('id')) }, function () { showGqlMessage('Xóa category thành công'); loadGqlCategoriesForSelect(); loadCategoryPage(); loadProductPage(); loadHomeProducts(); });
    });

    $(document).on('click', '.g-product-edit', function () {
        let item = JSON.parse($(this).attr('data-item').replace(/&#39;/g, "'"));
        $('#g-product-id').val(item.productId); $('#g-product-name').val(item.productName); $('#g-product-category').val(item.category.categoryId); $('#g-product-images').val(item.images || '');
        $('#g-product-price').val(item.unitPrice); $('#g-product-discount').val(item.discount); $('#g-product-quantity').val(item.quantity); $('#g-product-status').val(item.status); $('#g-product-description').val(item.description || '');
        window.scrollTo({ top: 1050, behavior: 'smooth' });
    });

    $(document).on('click', '.g-product-delete', function () {
        if (!confirm('Bạn có chắc muốn xóa product này?')) return;
        gqlRequest('mutation($id:ID!) { deleteProduct(id:$id) }', { id: String($(this).data('id')) }, function () { showGqlMessage('Xóa product thành công'); loadProductPage(); loadHomeProducts(); });
    });
});
