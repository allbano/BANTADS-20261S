CREATE KEYSPACE IF NOT EXISTS ms_auth
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

CREATE TABLE IF NOT EXISTS ms_auth.user_accounts (
    email text PRIMARY KEY,
    password_hash text,
    account_role text
);

INSERT INTO ms_auth.user_accounts (email, password_hash, account_role) VALUES ('cli1@bantads.com.br', '0da5cfc978e594f5a1fe7b6e15dac1c175a6779daea50c7e85650524cd03d395', 'CLIENTE');
INSERT INTO ms_auth.user_accounts (email, password_hash, account_role) VALUES ('cli2@bantads.com.br', '0da5cfc978e594f5a1fe7b6e15dac1c175a6779daea50c7e85650524cd03d395', 'CLIENTE');
INSERT INTO ms_auth.user_accounts (email, password_hash, account_role) VALUES ('cli3@bantads.com.br', '0da5cfc978e594f5a1fe7b6e15dac1c175a6779daea50c7e85650524cd03d395', 'CLIENTE');
INSERT INTO ms_auth.user_accounts (email, password_hash, account_role) VALUES ('cli4@bantads.com.br', '0da5cfc978e594f5a1fe7b6e15dac1c175a6779daea50c7e85650524cd03d395', 'CLIENTE');
INSERT INTO ms_auth.user_accounts (email, password_hash, account_role) VALUES ('cli5@bantads.com.br', '0da5cfc978e594f5a1fe7b6e15dac1c175a6779daea50c7e85650524cd03d395', 'CLIENTE');
INSERT INTO ms_auth.user_accounts (email, password_hash, account_role) VALUES ('ger1@bantads.com.br', '0da5cfc978e594f5a1fe7b6e15dac1c175a6779daea50c7e85650524cd03d395', 'GERENTE');
INSERT INTO ms_auth.user_accounts (email, password_hash, account_role) VALUES ('ger2@bantads.com.br', '0da5cfc978e594f5a1fe7b6e15dac1c175a6779daea50c7e85650524cd03d395', 'GERENTE');
INSERT INTO ms_auth.user_accounts (email, password_hash, account_role) VALUES ('ger3@bantads.com.br', '0da5cfc978e594f5a1fe7b6e15dac1c175a6779daea50c7e85650524cd03d395', 'GERENTE');
INSERT INTO ms_auth.user_accounts (email, password_hash, account_role) VALUES ('adm1@bantads.com.br', '0da5cfc978e594f5a1fe7b6e15dac1c175a6779daea50c7e85650524cd03d395', 'ADMINISTRADOR');
