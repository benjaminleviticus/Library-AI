const bookList = document.getElementById('bookList');
const searchInput = document.getElementById('bookSearch');
const genreSelect = document.getElementById('genreFilter');
const statusEl = document.getElementById('bookStatus');

const PLACEHOLDER_COVER = 'images/book-logo.webp';

// Shown only if the API is unreachable or the library is empty
const demoBooks = [
  {
    title: "The Hobbit",
    author: "J.R.R. Tolkien",
    description: "A fantasy adventure of Bilbo Baggins joining dwarves on a quest to reclaim their homeland from Smaug.",
    genre: "Fantasy",
    img: "https://covers.openlibrary.org/b/id/6979861-L.jpg"
  },
  {
    title: "Harry Potter",
    author: "J.K. Rowling",
    description: "The story of a young wizard, Harry Potter, and his adventures at Hogwarts.",
    genre: "Fantasy",
    img: "https://covers.openlibrary.org/b/id/7984916-L.jpg"
  },
  {
    title: "1984",
    author: "George Orwell",
    description: "A dystopian novel about a totalitarian regime controlling society.",
    genre: "Dystopian",
    img: "https://covers.openlibrary.org/b/id/7222246-L.jpg"
  }
];

let allBooks = [];

// ================== Load books ==================
async function loadBooks() {
  setStatus('Loading books…');
  try {
    const res = await fetch('/books/getAllBooks');
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const data = await res.json();
    allBooks = Array.isArray(data) ? data : [];
    if (allBooks.length === 0) {
      allBooks = demoBooks;
    }
  } catch (err) {
    console.warn('Could not load books from the API, showing demo books:', err);
    allBooks = demoBooks;
  }
  populateGenres();
  render();
}

function setStatus(msg) {
  if (statusEl) statusEl.textContent = msg;
}

// ================== Filtering ==================
function populateGenres() {
  if (!genreSelect) return;
  const genres = [...new Set(allBooks.map(b => (b.genre || '').trim()).filter(Boolean))].sort();
  genreSelect.replaceChildren(new Option('All genres', ''));
  genres.forEach(g => genreSelect.appendChild(new Option(g, g)));
}

function getFiltered() {
  const q = (searchInput?.value || '').trim().toLowerCase();
  const genre = genreSelect?.value || '';
  return allBooks.filter(b => {
    const matchesText = !q ||
      (b.title || '').toLowerCase().includes(q) ||
      (b.author || '').toLowerCase().includes(q);
    const matchesGenre = !genre || (b.genre || '').trim() === genre;
    return matchesText && matchesGenre;
  });
}

function render() {
  const books = getFiltered();
  bookList.replaceChildren(...books.map(createBookCard));
  setStatus(books.length === 0 ? 'No books match your search.' : '');
}

searchInput?.addEventListener('input', render);
genreSelect?.addEventListener('change', render);

// ================== Create Book Card (no innerHTML: titles are untrusted) ==================
function el(tag, className, text) {
  const node = document.createElement(tag);
  if (className) node.className = className;
  if (text !== undefined) node.textContent = text;
  return node;
}

function createBookCard(book) {
  const li = el('li', 'book-card');
  li.tabIndex = 0;
  li.setAttribute('role', 'button');
  li.setAttribute('aria-label', `${book.title || 'Untitled'} by ${book.author || 'unknown author'} — show summary`);

  const inner = el('div', 'card-inner');

  const front = el('div', 'card-front');
  const img = el('img', 'book-img');
  img.src = book.img || PLACEHOLDER_COVER;
  img.alt = `Cover of ${book.title || 'book'}`;
  img.loading = 'lazy';
  img.addEventListener('error', () => { img.src = PLACEHOLDER_COVER; }, { once: true });
  front.append(img, el('strong', '', book.title || 'Untitled'), el('div', 'details', book.author || ''));

  const back = el('div', 'card-back summary-box');
  back.appendChild(el('p', '', book.description || 'No description available.'));
  if (book.id != null && book.hasPdf) {
    const read = el('a', 'card-read', 'Read this book');
    read.href = `/reader.html?id=${encodeURIComponent(book.id)}`;
    read.addEventListener('click', e => e.stopPropagation());
    back.appendChild(read);
  }

  inner.append(front, back);
  li.appendChild(inner);
  return li;
}

// ================== Tap / keyboard support (registered once) ==================
bookList.addEventListener('click', (e) => {
  const li = e.target.closest('li.book-card');
  if (li) li.classList.toggle('show-summary');
});
bookList.addEventListener('keydown', (e) => {
  if (e.key !== 'Enter' && e.key !== ' ') return;
  const li = e.target.closest('li.book-card');
  if (!li || e.target !== li) return;
  e.preventDefault();
  li.classList.toggle('show-summary');
});

// ================== Particle Animation ==================
function startParticles() {
  const container = document.getElementById('particles');
  if (!container) return;
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;

  const count = window.innerWidth < 868 ? 12 : 28;
  const frag = document.createDocumentFragment();
  for (let i = 0; i < count; i++) {
    const p = document.createElement('div');
    p.className = 'particle';
    const size = Math.random() * 8 + 4;
    p.style.width = `${size}px`;
    p.style.height = `${size}px`;
    p.style.left = `${Math.random() * 100}vw`;
    p.style.top = `${100 + Math.random() * 120}vh`;
    p.style.animationDuration = `${20000 + Math.random() * 10000}ms`;
    p.style.animationDelay = `${Math.random() * 10000}ms`;
    frag.appendChild(p);
  }
  container.appendChild(frag);
}

// ================== Chatbot Integration (optional, never blocks the page) ==================
async function startChatbot() {
  try {
    const { default: Chatbot } = await import('https://cdn.jsdelivr.net/npm/flowise-embed@3.1.6/dist/web.js');
    Chatbot.init({
      chatflowid: "5aeda764-9d6a-449f-ae64-8105e6a0bf8c",
      // Flowise runs on port 8082 next to this app; override with window.FLOWISE_HOST if deployed elsewhere
      apiHost: window.FLOWISE_HOST || `${location.protocol}//${location.hostname}:8082`,
      theme: {
        button: { backgroundColor: "#f0cd0a", right: 24, bottom: 24, size: "large" },
        chatWindow: {
          title: "Library Assistant 🤖",
          titleColor: "#f0cd0a",
          backgroundColor: "#1b1b1b",
          welcomeMessage: "Hi there! I'm your Library Assistant. How can I help you today?",
          botMessage: { backgroundColor: "#2e2e2e", textColor: "#ffffff" },
          userMessage: { backgroundColor: "#f0cd0a", textColor: "#111111" },
        },
      },
    });
  } catch (err) {
    console.warn('Chatbot unavailable:', err);
  }
}

loadBooks();
startParticles();
startChatbot();
