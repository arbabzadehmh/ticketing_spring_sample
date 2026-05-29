let firstLoad = true;

function loadNotifications(page = 0) {
    const container = document.getElementById('notifications-table-container');
    const pageSizeElement = document.getElementById('pageSize');

    if (!container || !pageSizeElement) {
        console.log('%cPermissions table not found, skipping loadPermissions()', 'color: orange;');
        return;
    }

    const size = parseInt(pageSizeElement.value, 10);

    if (firstLoad) {
        firstLoad = false;

        console.log("web controller / fetch")

        let url = `/notifications?page=${encodeURIComponent(page)}&size=${encodeURIComponent(size)}&fragment=true`;


        fetch(url)
            .then(response => {
                if (!response.ok) throw new Error('خطا در دریافت داده‌ها');
                return response.text();
            })
            .then(html => {
                container.innerHTML = html;

                document.getElementById('pageSize').value = size;

            })
            .catch(error => showToast('danger', error.message || 'خطا در دریافت داده‌ها'));

        return;
    }

    console.log("rest controller / fetch")
    let apiUrl = `/rest/notifications?page=${encodeURIComponent(page)}&size=${encodeURIComponent(size)}`;

    const currentPage = page;

    fetch(apiUrl)
        .then(response => {
            if (!response.ok) throw new Error('خطا در دریافت داده‌ها');
            return response.json();
        })
        .then(data => {
            renderNotificationsTable(data.content);
            renderPagination(data);

            document.getElementById('pageSize').value = size;

            // اضافه: بعد از رندر pagination، دوباره روی همان صفحه بایست
            const paginationContainer = document.querySelector(".pagination");
            const activeLink = paginationContainer.querySelector(`.page-link[data-page='${currentPage}']`);
            if (activeLink) activeLink.parentElement.classList.add('active');
        })
        .catch(error => showToast('danger', error.message || 'خطا در دریافت داده‌ها'));
}

// -------------------- Render Table --------------------
function renderNotificationsTable(notifications) {
    const tableBody = document.querySelector("#notifications-table tbody");
    tableBody.innerHTML = '';

    notifications.forEach(n => {
        const row = document.createElement('tr');

        row.innerHTML = `
            <td>${n.title}</td>
            <td>${n.message}</td>
            <td>${n.read}</td>
            <td>${formatDateTime(n.createdAt)}</td>
            <td>
                <button class="btn btn-sm btn-info btn-open" data-link="${n.link}">
                    <i class="fas fa-newspaper"></i>
                </button>
            </td>
        `;

        tableBody.appendChild(row);
    });
}

// -------------------- Render Pagination --------------------
function renderPagination(data, currentPage = 0) { // اضافه کردن currentPage
    const paginationContainer = document.querySelector(".pagination");
    if (!paginationContainer) return;

    paginationContainer.innerHTML = '';

    for (let i = 0; i < data.totalPages; i++) {
        const li = document.createElement('li');
        li.className = 'page-item' + (i === currentPage ? ' active' : ''); // استفاده از currentPage
        const a = document.createElement('a');
        a.className = 'page-link';
        a.textContent = (i + 1);
        a.href = '#';
        a.dataset.page = i; // dataset-page برای delegation
        li.appendChild(a);
        paginationContainer.appendChild(li);
    }
}

// -------------------------------------------------------

function formatDateTime(dateTime) {
    if (!dateTime) return '-';

    if (typeof dateTime === 'string') {
        return dateTime.replace('T', ' ');
    }

    if (Array.isArray(dateTime)) {
        // [year, month, day, hour, minute, second, nano]
        const [y, m, d, h, min] = dateTime;
        return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')} ${String(h).padStart(2, '0')}:${String(min).padStart(2, '0')}`;
    }

    return '-';
}


// -------------------- Handle Server Response --------------------
async function handleResponse(response, mode) {
    const data = await response.json();

    if (!response.ok) {
        if (response.status === 400) {
            // خطاهای اعتبارسنجی
            displayValidationErrors(data, mode);
            throw new Error('Validation errors');
        }

        // سایر خطاها (۵۰۰ یا ...)، پیام در فیلد error است
        const errorMessage = data.error || 'خطای ناشناخته در سرور';
        showToast('danger', errorMessage);
        throw new Error(errorMessage);
    }

    // موفقیت (پیام در فیلد message است)
    return data;
}

// ---------------------------------------------------------
function getCsrfToken() {
    return document.querySelector("meta[name='_csrf']").content;
}

function getCsrfHeader() {
    return document.querySelector("meta[name='_csrf_header']").content;
}

async function secureFetch(url, options = {}) {

    options.headers = {
        ...(options.headers || {}),
        [getCsrfHeader()]: getCsrfToken()
    };

    return fetch(url, options);
}


// ----------------------------------------------------
document.addEventListener('DOMContentLoaded', () => {
    // بارگذاری اولیه
    loadNotifications();


    // --- delegation کلیک‌ها ---
    document.body.addEventListener('click', async (e) => {

        const pageLink = e.target.closest('.page-link');
        if (pageLink) {
            e.preventDefault();
            const pageAttr = pageLink.dataset.page;
            const page = pageAttr != null
                ? parseInt(pageAttr, 10)
                : (parseInt(pageLink.textContent, 10) - 1);

            if (!isNaN(page)) loadNotifications(page);
            return;
        }

        const btnView = e.target.closest('.btn-open');

        if (btnView) {

            e.preventDefault();

            const id = btnView.dataset.id;
            const link = btnView.dataset.link;

            const token = document.querySelector('meta[name="_csrf"]')?.content;
            const header = document.querySelector('meta[name="_csrf_header"]')?.content;

            try {

                const headers = {};

                if (token && header) {
                    headers[header] = token;
                }

                await secureFetch(`/rest/notifications/${id}/read`, {
                    method: 'POST',
                    headers: headers
                });

                // optional ui update
                const row = btnView.closest('tr');

                if (row) {
                    row.classList.remove('fw-bold');
                }

            } catch (err) {
                console.error('Notification read failed', err);
            }

            if (link) {
                window.location.href = link;
            }
        }
    });

    document.body.addEventListener('change', (e) => {
        if (e.target.id === 'pageSize') {
            loadNotifications(0);
        }
    });
});