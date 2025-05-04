create table public.password_recovery_request
(
    id              SERIAL PRIMARY KEY,
    email           TEXT NOT NULL,
    send_time       TIMESTAMP NOT NULL DEFAULT now(),
    expiry_time     TIMESTAMP NOT NULL DEFAULT (now() + INTERVAL '3 hours'),
    used            BOOLEAN NOT NULL DEFAULT FALSE,
    used_time       TIMESTAMP,
    hashed_id       VARCHAR(255) NOT NULL UNIQUE DEFAULT md5(random()::TEXT),
    disabled        BOOLEAN DEFAULT FALSE NOT NULL
);

COMMENT ON COLUMN public.password_recovery_request.id is
  'ID of the password recovery request';

COMMENT ON COLUMN public.password_recovery_request.email is
  'Email address of the user who requested password recovery';

COMMENT ON COLUMN public.password_recovery_request.send_time is
  'Timestamp when the recovery request was sent';

COMMENT ON COLUMN public.password_recovery_request.expiry_time is
    'Timestamp when the password recovery request expires';

COMMENT ON COLUMN public.password_recovery_request.used is
  'True if the password recovery link was already used';

COMMENT ON COLUMN public.password_recovery_request.used_time is
  'Timestamp when the password recovery link was used';

COMMENT ON COLUMN public.password_recovery_request.hashed_id
    IS 'A unique URL-safe hashed identifier used to reference the password recovery request in reset links.';

COMMENT ON COLUMN public.password_recovery_request.disabled
    IS 'If true, the password recovery request is disabled regardless of expiry time.';
