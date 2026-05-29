let firstLoad = true;

// -------------------- Load Tickets --------------------
function loadTickets(page = 0, extraParams = {}) {
    const container = document.getElementById('report-tickets-table-container');
    const pageSizeElement = document.getElementById('pageSize');
    const reportId = document.body.getAttribute('data-report-id');


    if (!container || !pageSizeElement) {
        console.log('%cTickets table not found, skipping loadTickets()', 'color: orange;');
        return;
    }

    if (!reportId) {
        showToast('danger', 'شناسه گزارش نامعتبر است');
        return;
    }


    const size = parseInt(pageSizeElement.value, 10) || 10;


    // --- بار اول: از WebController (fragment) ---
    if (firstLoad) {
        firstLoad = false;
        console.log("web controller / fetch");

        // برای fragment، اگر فیلترها خالی باشند از fragment=true استفاده می‌کنیم
        const url = `/reports/low-score/${reportId}?page=${encodeURIComponent(page)}&size=${encodeURIComponent(size)}&fragment=true`;
        fetch(url)
            .then(res => {
                if (!res.ok) throw new Error('خطا در دریافت داده‌ها');
                return res.text();
            })
            .then(html => {
                container.innerHTML = html;

                // برگردوندن انتخاب‌های کاربر
                const ps = document.getElementById('pageSize');
                if (ps) ps.value = size;

            })
            .catch(err => showToast('danger', err.message || 'خطا در دریافت داده‌ها'));
        return;
    }

    // --- دفعات بعد: RestController (JSON) ---
    console.log("rest controller / fetch");

    // build API url with both input-search and extraParams (extraParams overrides if set)
    const query = new URLSearchParams();
    query.set('page', page);
    query.set('size', size);


    const apiUrl = `/rest/reports/low-score/${reportId}?${query.toString()}`;

    const currentPage = page;

    fetch(apiUrl)
        .then(res => {
            if (!res.ok) throw new Error('خطا در دریافت داده‌ها');
            return res.json();
        })
        .then(data => {
            renderTicketsTable(data.content);
            renderPagination(data, currentPage);

            const ps = document.getElementById('pageSize');
            if (ps) ps.value = size;

            // بعد از رندر pagination، دوباره روی همان صفحه بایست
            const paginationContainer = document.querySelector(".pagination");
            if (paginationContainer) {
                const activeLink = paginationContainer.querySelector(`.page-link[data-page='${currentPage}']`);
                if (activeLink) activeLink.parentElement.classList.add('active');
            }
        })
        .catch(err => showToast('danger', err.message || 'خطا در دریافت داده‌ها'));
}

// -------------------- Render Table --------------------
function renderTicketsTable(tickets) {
    const tableBody = document.querySelector("#report-ticket-table tbody");
    if (!tableBody) return;

    tableBody.innerHTML = '';

    tickets.forEach(t => {


        const row = document.createElement('tr');

        const sectionName = t.sectionTitle ?? '-';
        const customerName = (t.customer && (t.customer.username || t.customer.fullName)) ? (t.customer.username || t.customer.fullName) : '-';
        const status = t.status || '-';
        const score = t.score || '-';


        // permissions in roles had buttons; here we prepare edit/delete/view with data-*
        row.innerHTML = `
      <td>${escapeHtml(t.title || '-')}</td>
      <td>${escapeHtml(sectionName)}</td>
      <td>${escapeHtml(customerName)}</td>
      <td>${escapeHtml(status)}</td>
      <td>${escapeHtml(score)}</td>
      <td>${formatDateTime(t.dateTime)}</td>
      
      <td>
        <button class="btn btn-sm btn-info btn-open" data-id="${t.id}">
          <i class="fas fa-envelope-open"></i>
        </button>
      </td>
    `;
        tableBody.appendChild(row);
    });
}

// -------------------- Render Pagination --------------------
function renderPagination(data, currentPage = 0) {
    const paginationContainer = document.querySelector(".pagination");
    if (!paginationContainer) return;

    paginationContainer.innerHTML = '';

    for (let i = 0; i < data.totalPages; i++) {
        const li = document.createElement('li');
        li.className = 'page-item' + (i === currentPage ? ' active' : '');
        const a = document.createElement('a');
        a.className = 'page-link';
        a.textContent = (i + 1);
        a.href = '#';
        a.dataset.page = i;
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
            displayValidationErrors(data, mode);
            throw new Error('Validation errors');
        }
        const errorMessage = data.error || data.message || 'خطای ناشناخته در سرور';
        showToast('danger', errorMessage);
        throw new Error(errorMessage);
    }
    return data;
}

// -------------------- Utilities --------------------
function escapeHtml(str) {
    if (str === null || str === undefined) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function escapeAttr(str) {
    return escapeHtml(str).replace(/"/g, '&quot;');
}


// -------------------- Global Delegation (single place) --------------------
document.addEventListener('DOMContentLoaded', () => {
    loadTickets();


    // delegation for clicks: pagination, view, edit, delete
    document.body.addEventListener('click', e => {
        const pageLink = e.target.closest('.page-link');
        if (pageLink) {
            e.preventDefault();
            const pageAttr = pageLink.dataset.page;
            const page = pageAttr != null ? parseInt(pageAttr, 10) : (parseInt(pageLink.textContent, 10) - 1);
            if (!isNaN(page)) loadTickets(page);
            return;
        }


        const btnView = e.target.closest('.btn-open');
        if (btnView) {
            const id = btnView.dataset.id;
            if (id) window.location.href = `/tickets/${id}`;
        }
    });

    document.body.addEventListener('change', e => {
        if (e.target.id === 'pageSize') {
            loadTickets(0);
        }
    });

});
