INSERT INTO publishers (name, cnpj, email, phone, address)
VALUES
    ('Horizonte Editorial', '12.345.678/0001-95', 'contato@horizonte.example', '(85) 91234-5678', 'Rua das Flores, 120, Fortaleza - CE'),
    ('Mundo Literário', '23.456.789/0001-95', 'contato@mundoliterario.example', '(85) 92345-6789', 'Avenida Central, 250, Fortaleza - CE'),
    ('Nova Página', '34.567.891/0001-84', 'contato@novapagina.example', '(85) 93456-7890', 'Rua das Letras, 310, Caucaia - CE'),
    ('Estrela do Saber', '45.678.912/0001-55', 'contato@estreladosaber.example', '(85) 94567-8901', 'Avenida Brasil, 450, Maracanaú - CE'),
    ('Ponto de Leitura', '56.789.023/0001-81', 'contato@pontodeleitura.example', '(85) 95678-9012', 'Rua da Cultura, 550, Eusébio - CE');

INSERT INTO users (
    name,
    email,
    phone,
    cpf,
    date_birth,
    address,
    is_admin,
    is_disabled,
    password
)
VALUES
    (
        'Lucas Almeida',
        'lucas.almeida@example.com',
        '(85) 91234-1111',
        '123.456.789-09',
        '2000-05-15',
        'Rua das Palmeiras, 100, Fortaleza - CE',
        FALSE,
        FALSE,
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
    ),
    (
        'Mariana Costa',
        'mariana.costa@example.com',
        '(85) 92345-2222',
        '234.567.890-92',
        '1998-08-20',
        'Avenida Santos Dumont, 200, Fortaleza - CE',
        FALSE,
        FALSE,
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
    ),
    (
        'Gabriel Santos',
        'gabriel.santos@example.com',
        '(85) 93456-3333',
        '345.678.901-75',
        '2002-03-10',
        'Rua Ceará, 300, Caucaia - CE',
        FALSE,
        FALSE,
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
    ),
    (
        'Ana Oliveira',
        'ana.oliveira@example.com',
        '(85) 94567-4444',
        '456.789.012-49',
        '1999-11-25',
        'Rua das Acácias, 400, Maracanaú - CE',
        FALSE,
        FALSE,
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
    ),
    (
        'Pedro Ferreira',
        'pedro.ferreira@example.com',
        '(85) 95678-5555',
        '567.890.123-03',
        '2001-07-12',
        'Avenida Principal, 500, Eusébio - CE',
        FALSE,
        FALSE,
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
    );

INSERT INTO books (
    title,
    isbn,
    genre,
    quantity,
    publisher_id,
    release_date
)
VALUES
    (
        'O Caminho das Estrelas',
        '9781234567897',
        'Fantasia',
        5,
        (SELECT id FROM publishers WHERE cnpj = '12.345.678/0001-95'),
        '2021-03-15'
    ),
    (
        'Além do Horizonte',
        '9782345678908',
        'Aventura',
        5,
        (SELECT id FROM publishers WHERE cnpj = '23.456.789/0001-95'),
        '2020-06-20'
    ),
    (
        'Segredos da Floresta',
        '9783456789019',
        'Mistério',
        4,
        (SELECT id FROM publishers WHERE cnpj = '34.567.891/0001-84'),
        '2022-01-10'
    ),
    (
        'O Universo do Conhecimento',
        '9784567890120',
        'Didático',
        5,
        (SELECT id FROM publishers WHERE cnpj = '45.678.912/0001-55'),
        '2019-09-05'
    ),
    (
        'Memórias de um Novo Dia',
        '9785678901231',
        'Drama',
        4,
        (SELECT id FROM publishers WHERE cnpj = '56.789.023/0001-81'),
        '2023-04-18'
    );

INSERT INTO rentals (
    users_id,
    books_id,
    start_date,
    end_date,
    status,
    was_returned,
    was_renewed
)
VALUES
    (
        (SELECT id FROM users WHERE cpf = '123.456.789-09'),
        (SELECT id FROM books WHERE isbn = '9781234567897'),
        CURRENT_DATE - 25,
        CURRENT_DATE - 11,
        'RETURNED',
        TRUE,
        FALSE
    ),
    (
        (SELECT id FROM users WHERE cpf = '234.567.890-92'),
        (SELECT id FROM books WHERE isbn = '9782345678908'),
        CURRENT_DATE - 20,
        CURRENT_DATE - 6,
        'RETURNED',
        TRUE,
        TRUE
    ),
    (
        (SELECT id FROM users WHERE cpf = '345.678.901-75'),
        (SELECT id FROM books WHERE isbn = '9783456789019'),
        CURRENT_DATE - 20,
        CURRENT_DATE - 6,
        'ACTIVE',
        FALSE,
        FALSE
    ),
    (
        (SELECT id FROM users WHERE cpf = '456.789.012-49'),
        (SELECT id FROM books WHERE isbn = '9784567890120'),
        CURRENT_DATE - 13,
        CURRENT_DATE + 1,
        'ACTIVE',
        FALSE,
        FALSE
    ),
    (
        (SELECT id FROM users WHERE cpf = '567.890.123-03'),
        (SELECT id FROM books WHERE isbn = '9785678901231'),
        CURRENT_DATE - 4,
        CURRENT_DATE + 10,
        'ACTIVE',
        FALSE,
        FALSE
    );
