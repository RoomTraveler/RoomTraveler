-- Add accommodation_type column to accommodations table if it doesn't exist
ALTER TABLE accommodations ADD COLUMN IF NOT EXISTS accommodation_type VARCHAR(50) NULL AFTER longitude;