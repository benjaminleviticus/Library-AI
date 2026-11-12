const params = new URLSearchParams(window.location.search);
const bookId = params.get("id");

const titleEl = document.getElementById("book-title");
const authorEl = document.getElementById("book-author");
const container = document.getElementById("page-container");
const pageCountEl = document.getElementById("page-count");
const prevBtn = document.getElementById("prev");
const nextBtn = document.getElementById("next");
const zoomInBtn = document.getElementById("zoom-in");
const zoomOutBtn = document.getElementById("zoom-out");

const storageKey = `reader:${bookId}`;
const saved = readSaved();

let pdfDoc = null;
let currentPage = saved.page || 1;
let scale = saved.scale || 1.3;
let renderToken = 0;

function readSaved() {
  try { return JSON.parse(localStorage.getItem(storageKey)) || {}; } catch { return {}; }
}
function save() {
  try { localStorage.setItem(storageKey, JSON.stringify({ page: currentPage, scale })); } catch { /* storage unavailable */ }
}

function showMessage(text) {
  const p = document.createElement("p");
  p.className = "reader-message";
  p.textContent = text;
  container.replaceChildren(p);
}

// Load book details + PDF
async function loadBookInfo() {
  if (!bookId) { titleEl.textContent = "No book selected"; return; }
  try {
    const res = await fetch(`/books/getBookById/${encodeURIComponent(bookId)}`);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const data = await res.json();
    titleEl.textContent = data.title || "Untitled Book";
    document.title = `${data.title || "Book"} – Reader`;
    authorEl.textContent = data.author ? `by ${data.author}` : "";
    loadPdf(`/books/${encodeURIComponent(bookId)}/pdf`);
  } catch {
    titleEl.textContent = "Book Not Found";
    showMessage("We couldn't find that book.");
  }
}

// Load PDF with PDF.js
async function loadPdf(url) {
  showMessage("Loading…");
  try {
    pdfDoc = await pdfjsLib.getDocument(url).promise;
    currentPage = Math.min(Math.max(currentPage, 1), pdfDoc.numPages);
    renderPage(currentPage);
  } catch (err) {
    showMessage(`No readable PDF is available for this book. (${err.message})`);
  }
}

// Render page (ignores stale renders if the user clicks quickly)
async function renderPage(num) {
  const token = ++renderToken;
  const page = await pdfDoc.getPage(num);
  if (token !== renderToken) return;

  const viewport = page.getViewport({ scale });
  const canvas = document.createElement("canvas");
  canvas.height = viewport.height;
  canvas.width = viewport.width;
  await page.render({ canvasContext: canvas.getContext("2d"), viewport }).promise;
  if (token !== renderToken) return;

  container.replaceChildren(canvas);
  prevBtn.disabled = num <= 1;
  nextBtn.disabled = num >= pdfDoc.numPages;
  pageCountEl.textContent = `Page ${num} of ${pdfDoc.numPages}`;
  save();
}

function go(delta) {
  if (!pdfDoc) return;
  const next = currentPage + delta;
  if (next < 1 || next > pdfDoc.numPages) return;
  currentPage = next;
  renderPage(currentPage);
}

function zoom(factor) {
  if (!pdfDoc) return;
  scale = Math.min(3, Math.max(0.6, +(scale * factor).toFixed(2)));
  renderPage(currentPage);
}

nextBtn.addEventListener("click", () => go(1));
prevBtn.addEventListener("click", () => go(-1));
zoomInBtn.addEventListener("click", () => zoom(1.2));
zoomOutBtn.addEventListener("click", () => zoom(1 / 1.2));
document.addEventListener("keydown", (e) => {
  if (e.key === "ArrowRight" || e.key === "PageDown") go(1);
  else if (e.key === "ArrowLeft" || e.key === "PageUp") go(-1);
  else if (e.key === "+" || e.key === "=") zoom(1.2);
  else if (e.key === "-") zoom(1 / 1.2);
});

window.addEventListener("DOMContentLoaded", loadBookInfo);
