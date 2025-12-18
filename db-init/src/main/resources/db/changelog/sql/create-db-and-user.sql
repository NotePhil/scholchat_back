-- Création de la base scholchat et de l'utilisateur applicatif scholchat
-- À exécuter en étant connecté avec un rôle ayant les droits CREATEDB/CREATEROLE (ex : postgres/admin)

DO '
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = ''scholchat'') THEN
    EXECUTE ''CREATE ROLE scholchat WITH LOGIN PASSWORD ''''scholchat'''''';
  END IF;
END;';

DO '
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = ''scholchat'') THEN
    EXECUTE ''CREATE DATABASE scholchat OWNER scholchat'';
  END IF;
END;';
