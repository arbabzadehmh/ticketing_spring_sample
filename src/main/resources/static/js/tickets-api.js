// ticket-api.js
let firstLoad = true;

// -------------------- Load Tickets --------------------
function loadTickets(page = 0, extraParams = {}) {
    const container = document.getElementById('tickets-table-container');
    const pageSizeElement = document.getElementById('pageSize');
    const searchTitleElement = document.getElementById('searchTitle');
    const searchCustomerElement = document.getElementById('searchCustomer');
    const searchStatusElement = document.getElementById('searchStatus');
    const searchSectionElement = document.getElementById('searchSection');

    const rawRoles = document.body.dataset.roles;

    const roles = rawRoles
        ? rawRoles.replace(/^\[|\]$/g, '').split(',').map(r => r.trim())
        : [];


    if (!container || !pageSizeElement) {
        console.log('%cTickets table not found, skipping loadTickets()', 'color: orange;');
        return;
    }

    const size = parseInt(pageSizeElement.value, 10) || 10;
    const title = searchTitleElement ? searchTitleElement.value.trim() : '';
    const customer = searchCustomerElement ? searchCustomerElement.value.trim() : '';
    const status = searchStatusElement ? searchStatusElement.value.trim() : '';
    const sectionId = searchSectionElement ? searchSectionElement.value : '';

    // merge extraParams (from filter modal) - prefer explicit extraParams if provided
    const paramsFromExtra = Object.assign({}, {
        page,
        size
    }, extraParams);

    // --- بار اول: از WebController (fragment) ---
    if (firstLoad) {
        firstLoad = false;
        console.log("web controller / fetch");

        // برای fragment، اگر فیلترها خالی باشند از fragment=true استفاده می‌کنیم
        const url = `/tickets?page=${encodeURIComponent(page)}&size=${encodeURIComponent(size)}&fragment=true`;
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

                const newSearchTitle = document.getElementById('searchTitle');
                if (newSearchTitle && searchTitleElement) newSearchTitle.value = title;

                const newSearchCustomer = document.getElementById('searchCustomer');
                if (newSearchCustomer && searchCustomerElement) newSearchCustomer.value = customer;

                const newSearchStatus = document.getElementById('searchStatus');
                if (newSearchStatus && searchStatusElement) newSearchStatus.value = status;

                const newSearchSection = document.getElementById('searchSection');
                if (newSearchSection && searchSectionElement) newSearchSection.value = sectionId;

                // اگر فرم‌های داخل مودال‌ها وجود دارن، submitشون رو یکبار bind می‌کنیم
                const createForm = document.getElementById('ticketCreateForm');
                if (createForm && !createForm.dataset.bound) {
                    createForm.addEventListener('submit', handleCreateTicketSubmit);
                    createForm.dataset.bound = 'true';
                }

                const editForm = document.getElementById('ticketEditForm');
                if (editForm && !editForm.dataset.bound) {
                    editForm.addEventListener('submit', handleEditTicketSubmit);
                    editForm.dataset.bound = 'true';
                }

                // bind any one-time init (like filling selects for create modal)
                const createModal = document.getElementById('ticketCreateModal');
                if (createModal && !createModal.dataset.bound) {
                    createModal.addEventListener('show.bs.modal', () => {
                    });
                    createModal.dataset.bound = 'true';
                }

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

    // prefer explicit UI inputs unless extraParams include same key
    if (!('title' in extraParams) && title) query.set('title', title);
    if (!('customer' in extraParams) && customer) query.set('customer', customer);
    if (!('status' in extraParams) && status) query.set('status', status);
    if (!('sectionId' in extraParams) && sectionId) query.set('sectionId', sectionId);

    // add any extra params (from filter modal)
    for (const k in extraParams) {
        if (extraParams[k] !== null && extraParams[k] !== undefined && extraParams[k] !== '') {
            query.set(k, extraParams[k]);
        }
    }

    const apiUrl = `/rest/tickets?${query.toString()}`;

    const currentPage = page;

    fetch(apiUrl)
        .then(res => {
            if (!res.ok) throw new Error('خطا در دریافت داده‌ها');
            return res.json();
        })
        .then(data => {
            renderTicketsTable(data.content, roles);
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
function renderTicketsTable(tickets, roles) {
    const tableBody = document.querySelector("#tickets-table tbody");
    if (!tableBody) return;

    tableBody.innerHTML = '';

    tickets.forEach(t => {

        const canEdit = roles.includes("TICKET_EDIT");
        const canDelete = roles.includes("TICKET_DELETE");


        const row = document.createElement('tr');

        const sectionName = t.section?.title ?? t.section?.parentSection?.title ?? '-';
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
        
      <td>
      ${canEdit ? `
        <button class="btn btn-sm btn-warning btn-edit" 
            data-id="${t.id}"
            data-title="${escapeAttr(t.title || '')}"
            data-section-id="${t.section ? t.section.id : ''}"
            data-status="${escapeAttr(status)}">
          <i class="fas fa-edit"></i>
        </button>
        ` : ""}
        
        ${canDelete ? `
        <button class="btn btn-sm btn-danger btn-delete"
            data-id="${t.id}">
          <i class="fas fa-trash"></i>
        </button>
        ` : ""}
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

// -------------------- Create Ticket --------------------
async function handleCreateTicketSubmit(e) {
    e.preventDefault();
    clearValidationErrors();

    const ticket = {
        title: document.getElementById('createTitle').value.trim(),
        sectionId: document.getElementById('selectSection').value,
        content: document.getElementById('createContent').value.trim()
    };

    try {
        const response = await fetch('/rest/tickets', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(ticket)
        });

        const data = await handleResponse(response, 'create');

        bootstrap.Modal.getInstance(document.getElementById('ticketCreateModal'))?.hide();
        showToast('success', data.message || 'تیکت با موفقیت ثبت شد');
        loadTickets();

    } catch (error) {
        if (error.message !== 'Validation errors') {
            console.error('Ticket creation error:', error);
            showToast('danger', error.message || 'خطا در ثبت تیکت');
        }
    }
}

// -------------------- Edit Ticket --------------------
async function handleEditTicketSubmit(e) {
    e.preventDefault();
    clearValidationErrors();

    const id = document.getElementById('editTicketId').value;
    const ticket = {
        title: document.getElementById('editTitle').value.trim(),
        sectionId: document.getElementById('editSection').value,
        status: document.getElementById('editStatus').value.trim()
    };

    try {
        const response = await fetch(`/rest/tickets/${encodeURIComponent(id)}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(ticket)
        });

        const data = await handleResponse(response, 'edit');

        bootstrap.Modal.getInstance(document.getElementById('ticketEditModal'))?.hide();
        showToast('success', data.message || 'تیکت با موفقیت ویرایش شد');
        loadTickets();

    } catch (error) {
        if (error.message !== 'Validation errors') {
            console.error('Ticket edition error:', error);
            showToast('danger', error.message || 'خطا در ویرایش تیکت');
        }
    }
}

// -------------------- Delete Ticket --------------------
async function handleTicketDelete(e) {
    const btn = e.target.closest('.btn-delete, .btn-danger');
    if (!btn) return;
    const confirmTextElem = document.getElementById('deleteConfirmText');
    const confirmText = confirmTextElem ? confirmTextElem.textContent.trim() : (btn.dataset.confirmText || 'آیا اطمینان دارید؟');
    if (!confirm(confirmText)) return;

    const id = btn.dataset.id;
    try {
        const response = await fetch(`/rest/tickets/${encodeURIComponent(id)}`, { method: 'DELETE' });
        const data = await handleResponse(response, 'delete');
        showToast('success', data.message || 'تیکت با موفقیت حذف شد');
        loadTickets();
    } catch (error) {
        if (error.message !== 'Validation errors') {
            console.error('Ticket deletion error:', error);
            showToast('danger', error.message || 'خطا در حذف تیکت');
        }
    }
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

// -------------------- Validation helpers --------------------
function displayValidationErrors(errors, mode) {
    clearValidationErrors();

    for (const field in errors) {
        const cleanField = field.includes('.') ? field.split('.').pop() : field;

        let input = null;
        if (mode === 'edit') input = document.getElementById('edit' + capitalize(cleanField));
        else if (mode === 'create') input = document.getElementById('create' + capitalize(cleanField));
        if (!input) input = document.getElementById(cleanField);

        if (input) {
            input.classList.add('is-invalid');
            const errorDiv = document.createElement('div');
            errorDiv.className = 'invalid-feedback';
            errorDiv.textContent = errors[field];
            input.parentNode.appendChild(errorDiv);
        }
    }
}

function clearValidationErrors() {
    document.querySelectorAll('.is-invalid').forEach(i => i.classList.remove('is-invalid'));
    document.querySelectorAll('.invalid-feedback').forEach(f => f.remove());
}

function capitalize(str) { return str.charAt(0).toUpperCase() + str.slice(1); }

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

    // bind create/edit forms if present (prevent double bind)
    const createForm = document.getElementById('ticketCreateForm');
    if (createForm && !createForm.dataset.bound) {
        createForm.addEventListener('submit', handleCreateTicketSubmit);
        createForm.dataset.bound = 'true';
    }
    const editForm = document.getElementById('ticketEditForm');
    if (editForm && !editForm.dataset.bound) {
        editForm.addEventListener('submit', handleEditTicketSubmit);
        editForm.dataset.bound = 'true';
    }

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

        const btnDelete = e.target.closest('.btn-delete, .btn-danger');
        if (btnDelete) { handleTicketDelete(e); return; }

        const btnEdit = e.target.closest('.btn-edit');
        if (btnEdit) {
            const row = btnEdit.closest('tr');
            // set edit modal fields
            const id = btnEdit.dataset.id || '';
            document.getElementById('editTicketId').value = id;
            document.getElementById('editTitle').value = btnEdit.dataset.title ? btnEdit.dataset.title : (row ? row.cells[1].textContent.trim() : '');
            document.getElementById('editStatus').value = btnEdit.dataset.status || '';
            document.getElementById('editSection').value = btnEdit.dataset.sectionId || '';

            new bootstrap.Modal(document.getElementById('ticketEditModal')).show();
            return;
        }

        const btnView = e.target.closest('.btn-open');
        if (btnView) {
            const id = btnView.dataset.id;
            if (id) window.location.href = `/tickets/${id}`;
            return;
        }
    });

    // input listeners (debounced search)
    const debouncedLoad = debounce(() => loadTickets(0), 700);
    document.body.addEventListener('input', e => {
        if (['searchTitle', 'searchCustomer'].includes(e.target.id)) debouncedLoad();
    });
    document.body.addEventListener('change', e => {
        if (['searchStatus', 'searchSection', 'pageSize', 'sortBy'].includes(e.target.id)) loadTickets(0);
    });

    // optional: filter modal submit (if exists) -> use extraParams
    const ticketFilterForm = document.getElementById('ticketFilterForm');
    const ticketFilterModal = document.getElementById('ticketFilterModal');
    if (ticketFilterForm && !ticketFilterForm.dataset.bound) {
        ticketFilterForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const params = {
                dateFrom: document.getElementById('filterDateFrom')?.value,
                dateTo: document.getElementById('filterDateTo')?.value,
                status: document.getElementById('filterStatus')?.value,
                scoreLessThan: document.getElementById('filterScoreLessThan')?.value,
                customer: document.getElementById('filterCustomer')?.value,
                sectionId: document.getElementById('filterSection')?.value,
                title: document.getElementById('filterTitle')?.value
            };
            loadTickets(0, params);
            const modal = bootstrap.Modal.getInstance(ticketFilterModal);
            if (modal) modal.hide();
        });
        ticketFilterForm.dataset.bound = 'true';
    }

    // reset button for filter form
    const resetBtn = document.getElementById('filterResetBtn');
    if (resetBtn && ticketFilterForm) {
        resetBtn.addEventListener('click', () => ticketFilterForm.reset());
    }
});

// -------------------- Debounce Helper --------------------
function debounce(fn, delay) {
    let timeoutId;
    return function (...args) {
        clearTimeout(timeoutId);
        timeoutId = setTimeout(() => fn.apply(this, args), delay);
    };
}
