CREATE TABLE
IF
	NOT EXISTS "chapter" (
		"id" TEXT NOT NULL,
		"bookId" TEXT,
		"name" TEXT,
		"content" TEXT,
		"urls" TEXT,
		"updateTime" INTEGER,
		"index" INTEGER,
		"wordSum" INTEGER,
	PRIMARY KEY ( "id" ) 
	);