// Shared navigation behaviour (hamburger menu)
(() => {
  const toggle = document.querySelector('.hamburger');
  const links = document.querySelector('.nav-links');
  if (!toggle || !links) return;

  const setOpen = (open) => {
    links.classList.toggle('show', open);
    toggle.setAttribute('aria-expanded', String(open));
  };

  toggle.addEventListener('click', () => setOpen(!links.classList.contains('show')));
  links.addEventListener('click', (e) => { if (e.target.closest('a')) setOpen(false); });
  document.addEventListener('keydown', (e) => { if (e.key === 'Escape') setOpen(false); });
  document.addEventListener('click', (e) => {
    if (!e.target.closest('nav')) setOpen(false);
  });
})();
