// ==================== مدیریت تم ====================
function toggleTheme() {
    const body = document.body;
    const newTheme = body.getAttribute('data-bs-theme') === 'dark' ? 'light' : 'dark';
    body.setAttribute('data-bs-theme', newTheme);
    localStorage.setItem('theme', newTheme);
    updateThemeIcon(newTheme);
}

function updateThemeIcon(theme) {
    const icon = document.getElementById('themeIcon');
    if (!icon) return;

    icon.classList.remove('fa-moon', 'fa-sun');

    if (theme === 'dark') {
        icon.classList.add('fa-sun');
    } else {
        icon.classList.add('fa-moon');
    }

    // مطمئن شو کلاس 'fas' یا 'fa' هم هست (با توجه به نسخه فونت آوسام)
    if (!icon.classList.contains('fas')) {
        icon.classList.add('fas');
    }
}

// ==================== مدیریت سایدبار ====================
function toggleSidebar() {
    const sidebar = document.querySelector('.sidebar');
    if (sidebar) {
        sidebar.classList.toggle('active');
    }
}

// ==================== مدیریت زبان ====================

function initLanguageSwitcher() {
    const languageSwitcher = document.getElementById('languageSwitcher');
    if (languageSwitcher) {
        languageSwitcher.addEventListener('change', function () {
            const selectedLang = this.value;
            fetch('/admin/change-language', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: `lang=${selectedLang}`
            }).then(() => location.reload());
        });
    }
}

// ==================== مقداردهی اولیه ====================
document.addEventListener('DOMContentLoaded', () => {
    const savedTheme = localStorage.getItem('theme') || 'light';
    document.body.setAttribute('data-bs-theme', savedTheme);
    updateThemeIcon(savedTheme);
    initLanguageSwitcher();

    const themeSwitch = document.getElementById('themeSwitch');
    if (themeSwitch) {
        themeSwitch.addEventListener('click', toggleTheme);
    }

    const sidebarToggleBtn = document.getElementById('sidebarToggleBtn');
    if (sidebarToggleBtn) {
        sidebarToggleBtn.addEventListener('click', toggleSidebar);
    }

    document.body.style.overflowY = 'hidden';

    initNotifications();

});

// ===================================================================
function initNotifications() {

    document.querySelectorAll('.notification-item').forEach(item => {

        item.addEventListener('click', async function (e) {

            e.preventDefault();

            const id = this.dataset.id;
            const link = this.href;

            if (!id) {
                if (link) window.location.href = link;
                return;
            }

            const token = document.querySelector('meta[name="_csrf"]')?.content;
            const header = document.querySelector('meta[name="_csrf_header"]')?.content;

            try {

                const headers = {};

                if (token && header) {
                    headers[header] = token;
                }

                await fetch(`/rest/notifications/${id}/read`, {
                    method: 'POST',
                    headers: headers
                });

                // UI update
                this.classList.remove('fw-bold');

                const badge = document.querySelector('.notification-badge');

                if (badge) {

                    let count = parseInt(badge.innerText, 10);

                    if (count <= 1) {
                        badge.remove();
                    } else {
                        badge.innerText = count - 1;
                    }
                }

            } catch (err) {
                console.error('Notification read failed', err);
            }

            // navigation AFTER fetch
            if (link && link !== '#') {
                window.location.href = link;
            }

        });

    });
}
