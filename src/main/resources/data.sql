MERGE INTO mpa (id, name) KEY(name) VALUES (1, 'G');
MERGE INTO mpa (id, name) KEY(name) VALUES (3, 'PG');
MERGE INTO mpa (id, name) KEY(name) VALUES (4, 'PG-13');
MERGE INTO mpa (id, name) KEY(name) VALUES (5, 'R');
MERGE INTO mpa (id, name) KEY(name) VALUES (2, 'NC-17');

MERGE INTO genres (id, name) KEY(name) VALUES (4, 'Комедия');
MERGE INTO genres (id, name) KEY(name) VALUES (3, 'Драма');
MERGE INTO genres (id, name) KEY(name) VALUES (5, 'Мультфильм');
MERGE INTO genres (id, name) KEY(name) VALUES (6, 'Триллер');
MERGE INTO genres (id, name) KEY(name) VALUES (2, 'Документальный');
MERGE INTO genres (id, name) KEY(name) VALUES (1, 'Боевик');