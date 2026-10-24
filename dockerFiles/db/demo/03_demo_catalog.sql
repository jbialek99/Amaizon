BEGIN;

DO $catalog$
DECLARE
    pl_id bigint;
    us_id bigint;
    category_id bigint;
    category_pl_id bigint;
    category_us_id bigint;
    backpack_id bigint;
    mug_id bigint;
    product_id bigint;
    pc_id bigint;
    market_record record;
    item_record record;
BEGIN
    IF (SELECT COUNT(*) FROM countries) <> 2
       OR (SELECT COUNT(*) FROM currencies) <> 2
       OR (SELECT COUNT(*) FROM domains) <> 2 THEN
        RAISE EXCEPTION 'PL/US market seed must be initialized first';
    END IF;

    IF EXISTS (SELECT 1 FROM categories)
       OR EXISTS (SELECT 1 FROM category_countries)
       OR EXISTS (SELECT 1 FROM category_translations)
       OR EXISTS (SELECT 1 FROM products)
       OR EXISTS (SELECT 1 FROM product_countries)
       OR EXISTS (SELECT 1 FROM product_translations)
       OR EXISTS (SELECT 1 FROM product_prices)
       OR EXISTS (SELECT 1 FROM product_stocks) THEN
        RAISE EXCEPTION 'Catalog tables must be empty';
    END IF;

    SELECT id INTO STRICT pl_id
    FROM countries WHERE code = 'PL' AND status = 'ACTIVE';

    SELECT id INTO STRICT us_id
    FROM countries WHERE code = 'US' AND status = 'ACTIVE';

    INSERT INTO categories (
        stable_id, name, image_url, status,
        display_order, created_at, updated_at
    )
    VALUES (
        gen_random_uuid(), 'Accessories',
        'https://picsum.photos/id/250/800/800',
        'ACTIVE', 1, now(), now()
    )
    RETURNING id INTO category_id;

    INSERT INTO category_countries (
        stable_id, category_id, country_id,
        status, created_at, updated_at
    )
    VALUES (
        gen_random_uuid(), category_id, pl_id,
        'ACTIVE', now(), now()
    )
    RETURNING id INTO category_pl_id;

    INSERT INTO category_countries (
        stable_id, category_id, country_id,
        status, created_at, updated_at
    )
    VALUES (
        gen_random_uuid(), category_id, us_id,
        'ACTIVE', now(), now()
    )
    RETURNING id INTO category_us_id;

    INSERT INTO category_translations (
        stable_id, category_country_id,
        language_code, name, description, slug,
        created_at, updated_at
    )
    VALUES
    (
        gen_random_uuid(), category_pl_id,
        'pl-PL', 'Akcesoria',
        'Praktyczne akcesoria do codziennego uzytku.',
        'akcesoria', now(), now()
    ),
    (
        gen_random_uuid(), category_us_id,
        'en-US', 'Accessories',
        'Practical accessories for everyday use.',
        'accessories', now(), now()
    );

    INSERT INTO products (
        stable_id, category_id, brand,
        global_code, image_url, status,
        created_at, updated_at
    )
    VALUES (
        gen_random_uuid(), category_id,
        'North', 'BP123456',
        'https://picsum.photos/id/250/800/800',
        'ACTIVE', now(), now()
    )
    RETURNING id INTO backpack_id;

    INSERT INTO products (
        stable_id, category_id, brand,
        global_code, image_url, status,
        created_at, updated_at
    )
    VALUES (
        gen_random_uuid(), category_id,
        'Thermo', 'TM123456',
        'https://picsum.photos/id/30/800/800',
        'ACTIVE', now(), now()
    )
    RETURNING id INTO mug_id;

    FOR market_record IN
        SELECT *
        FROM (
            VALUES
            (pl_id, 'pl-PL', 'PL'),
            (us_id, 'en-US', 'US')
        ) AS markets(country_id, language_code, market_code)
    LOOP
        FOR item_record IN
            SELECT *
            FROM (
                VALUES
                (
                    backpack_id, 'BACKPACK',
                    149.99::numeric, 39.99::numeric, 25
                ),
                (
                    mug_id, 'MUG',
                    89.90::numeric, 24.90::numeric, 40
                )
            ) AS items(
                product_id, item_type,
                pl_price, us_price, available
            )
        LOOP
            product_id := item_record.product_id;

            INSERT INTO product_countries (
                stable_id, product_id, country_id,
                status, created_at, updated_at
            )
            VALUES (
                gen_random_uuid(), product_id,
                market_record.country_id,
                'ACTIVE', now(), now()
            )
            RETURNING id INTO pc_id;

            INSERT INTO product_prices (
                stable_id, product_country_id,
                price, status, valid_to,
                created_at, updated_at
            )
            VALUES (
                gen_random_uuid(), pc_id,
                CASE
                    WHEN market_record.market_code = 'PL'
                    THEN item_record.pl_price
                    ELSE item_record.us_price
                END,
                'CURRENT', NULL, now(), now()
            );

            INSERT INTO product_stocks (
                stable_id, product_country_id,
                quantity_available, quantity_reserved,
                created_at, updated_at
            )
            VALUES (
                gen_random_uuid(), pc_id,
                item_record.available, 0,
                now(), now()
            );

            IF item_record.item_type = 'BACKPACK' THEN
                IF market_record.market_code = 'PL' THEN
                    INSERT INTO product_translations (
                        stable_id, product_country_id,
                        language_code, name, description, slug,
                        created_at, updated_at
                    )
                    VALUES (
                        gen_random_uuid(), pc_id,
                        'pl-PL', 'Plecak miejski',
                        'Lekki plecak do pracy i na wycieczki.',
                        'plecak-miejski', now(), now()
                    );
                ELSE
                    INSERT INTO product_translations (
                        stable_id, product_country_id,
                        language_code, name, description, slug,
                        created_at, updated_at
                    )
                    VALUES (
                        gen_random_uuid(), pc_id,
                        'en-US', 'Urban Backpack',
                        'Lightweight backpack for everyday use.',
                        'urban-backpack', now(), now()
                    );
                END IF;
            ELSE
                IF market_record.market_code = 'PL' THEN
                    INSERT INTO product_translations (
                        stable_id, product_country_id,
                        language_code, name, description, slug,
                        created_at, updated_at
                    )
                    VALUES (
                        gen_random_uuid(), pc_id,
                        'pl-PL', 'Kubek termiczny',
                        'Kubek do cieplych i zimnych napojow.',
                        'kubek-termiczny', now(), now()
                    );
                ELSE
                    INSERT INTO product_translations (
                        stable_id, product_country_id,
                        language_code, name, description, slug,
                        created_at, updated_at
                    )
                    VALUES (
                        gen_random_uuid(), pc_id,
                        'en-US', 'Thermal Mug',
                        'Reusable mug for hot and cold drinks.',
                        'thermal-mug', now(), now()
                    );
                END IF;
            END IF;
        END LOOP;
    END LOOP;
END
$catalog$;

COMMIT;
