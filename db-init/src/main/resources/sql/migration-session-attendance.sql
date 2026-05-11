-- Migration to add session attendance tracking table
-- This table tracks who was expected to attend and who actually joined sessions

CREATE TABLE IF NOT EXISTS ressources.session_attendance (
    id VARCHAR(255) PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    cours_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('EXPECTED', 'JOINED', 'LEFT', 'COMPLETED')),
    joined_at TIMESTAMP,
    left_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_session_attendance_user 
        FOREIGN KEY (user_id) REFERENCES ressources.utilisateurs(id) ON DELETE CASCADE,
    CONSTRAINT fk_session_attendance_cours 
        FOREIGN KEY (cours_id) REFERENCES ressources.cours(id) ON DELETE CASCADE,
    
    UNIQUE(session_id, user_id)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_session_attendance_session_id ON ressources.session_attendance(session_id);
CREATE INDEX IF NOT EXISTS idx_session_attendance_user_id ON ressources.session_attendance(user_id);
CREATE INDEX IF NOT EXISTS idx_session_attendance_cours_id ON ressources.session_attendance(cours_id);
CREATE INDEX IF NOT EXISTS idx_session_attendance_status ON ressources.session_attendance(status);