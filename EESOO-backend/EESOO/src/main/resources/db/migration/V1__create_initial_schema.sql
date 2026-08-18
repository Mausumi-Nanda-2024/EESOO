-- Initial EESOO database schema

CREATE TABLE public.auth_sessions (
    created_at timestamp(6) with time zone NOT NULL,
    expires_at timestamp(6) with time zone NOT NULL,
    last_rotated_at timestamp(6) with time zone,
    revoked_at timestamp(6) with time zone,
    version bigint NOT NULL,
    device_install_id uuid NOT NULL,
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    refresh_token_hash character varying(512) NOT NULL,
    status character varying(255) NOT NULL,
    CONSTRAINT auth_sessions_pkey PRIMARY KEY (id)
);

CREATE TABLE public.device_installs (
    first_seen_at timestamp(6) with time zone NOT NULL,
    last_seen_at timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    device_id character varying(255),
    device_id_nullable_reason character varying(255),
    install_id character varying(255) NOT NULL,
    os_version character varying(255) NOT NULL,
    platform character varying(255) NOT NULL,
    CONSTRAINT device_installs_pkey PRIMARY KEY (id),
    CONSTRAINT device_installs_device_id_key UNIQUE (device_id),
    CONSTRAINT device_installs_install_id_key UNIQUE (install_id)
);

CREATE TABLE public.event_publication (
    completion_date timestamp(6) with time zone,
    publication_date timestamp(6) with time zone,
    id uuid NOT NULL,
    event_type character varying(255),
    listener_id character varying(255),
    serialized_event character varying(255),
    CONSTRAINT event_publication_pkey PRIMARY KEY (id)
);

CREATE TABLE public.pin_reset_attempts (
    failed_attempts integer NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    expires_at timestamp(6) with time zone NOT NULL,
    mobile_confirmed_at timestamp(6) with time zone,
    pin_issued_at timestamp(6) with time zone,
    version bigint NOT NULL,
    device_install_id uuid NOT NULL,
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    status character varying(30) NOT NULL,
    CONSTRAINT pin_reset_attempts_pkey PRIMARY KEY (id),
    CONSTRAINT uk_pin_reset_attempt_user_device
        UNIQUE (user_id, device_install_id)
);

CREATE TABLE public.user_device_links (
    linked_at timestamp(6) with time zone NOT NULL,
    unlinked_at timestamp(6) with time zone,
    device_install_id uuid NOT NULL,
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    linked_device_type character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    CONSTRAINT user_device_links_pkey PRIMARY KEY (id)
);

CREATE TABLE public.users (
    user_registered_at timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    email character varying(255),
    first_name character varying(255) NOT NULL,
    last_name character varying(255) NOT NULL,
    phone_number character varying(255) NOT NULL,
    pin character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    username character varying(255) NOT NULL,
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT users_email_key UNIQUE (email),
    CONSTRAINT users_phone_number_key UNIQUE (phone_number),
    CONSTRAINT users_username_key UNIQUE (username)
);