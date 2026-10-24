BEGIN;

DO $seed$
DECLARE
    pln_id bigint;
    usd_id bigint;
    pl_id bigint;
    us_id bigint;
BEGIN
    IF EXISTS (SELECT 1 FROM currencies)
       OR EXISTS (SELECT 1 FROM countries)
       OR EXISTS (SELECT 1 FROM domains) THEN
        RAISE EXCEPTION 'Expected empty market configuration tables';
    END IF;

    INSERT INTO currencies (
        stable_id, name, code, symbol, created_at, updated_at
    )
    VALUES (
        gen_random_uuid(), 'Polish Zloty', 'PLN',
        U&'\017C\0142', now(), now()
    )
    RETURNING id INTO pln_id;

    INSERT INTO currencies (
        stable_id, name, code, symbol, created_at, updated_at
    )
    VALUES (
        gen_random_uuid(), 'US Dollar', 'USD',
        '$', now(), now()
    )
    RETURNING id INTO usd_id;

    INSERT INTO countries (
        stable_id, name, code, language_code,
        status, currency_id, created_at, updated_at
    )
    VALUES (
        gen_random_uuid(), 'Poland', 'PL', 'pl-PL',
        'ACTIVE', pln_id, now(), now()
    )
    RETURNING id INTO pl_id;

    INSERT INTO countries (
        stable_id, name, code, language_code,
        status, currency_id, created_at, updated_at
    )
    VALUES (
        gen_random_uuid(), 'United States', 'US', 'en-US',
        'ACTIVE', usd_id, now(), now()
    )
    RETURNING id INTO us_id;

    INSERT INTO domains (
        stable_id, name, cc_tld, status,
        country_id, created_at, updated_at
    )
    VALUES (
        gen_random_uuid(), 'amaizon.pl', 'pl',
        'ACTIVE', pl_id, now(), now()
    );

    INSERT INTO domains (
        stable_id, name, cc_tld, status,
        country_id, created_at, updated_at
    )
    VALUES (
        gen_random_uuid(), 'amaizon.com', 'com',
        'ACTIVE', us_id, now(), now()
    );
END
$seed$;

COMMIT;
