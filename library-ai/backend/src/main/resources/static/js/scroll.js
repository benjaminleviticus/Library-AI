window.addEventListener('DOMContentLoaded', () => {
  const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  /* ================== Reveal .animate elements ================== */
  const animateElements = document.querySelectorAll('.animate');
  if (reduceMotion || !('IntersectionObserver' in window)) {
    animateElements.forEach(el => el.classList.add('show'));
  } else {
    const observer = new IntersectionObserver(entries => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add('show');
          observer.unobserve(entry.target);
        }
      });
    }, { threshold: 0.2 });

    animateElements.forEach((el, index) => {
      el.style.transitionDelay = `${index * 0.1}s`;
      observer.observe(el);
    });
  }

  /* ================== Hand-book pop-out scroll animation ================== */
  const handSection = document.querySelector('.hand');
  const handImg = document.querySelector('.hand-img');
  if (!handSection || !handImg) return;

  if (reduceMotion) {
    handImg.style.opacity = 1;
    return;
  }

  let ticking = false;
  const update = () => {
    const y = window.scrollY;
    handSection.classList.toggle('scrolled', y > 50);

    const scale = 1 + Math.min(y / 800, 0.5);       // up to 1.5x
    const opacity = Math.min(y / 300, 1);           // fades in
    const translateY = Math.min(y / 8, 40);         // subtle float

    handImg.style.transform = `translate(-50%, -${translateY}px) scale(${scale})`;
    handImg.style.opacity = opacity;
    ticking = false;
  };

  window.addEventListener('scroll', () => {
    if (!ticking) {
      ticking = true;
      requestAnimationFrame(update);
    }
  }, { passive: true });
});
