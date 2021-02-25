CREATE TABLE
IF
	NOT EXISTS "book" (
		"id" TEXT NOT NULL,
		"source" TEXT,
		"name" TEXT,
		"author" TEXT,
		"categoryName" TEXT,
		"cover" TEXT,
		"shortSummary" TEXT,
		"cpName" TEXT,
		"lastChapterName" TEXT,
		"lastChapterUpdateTime" INTEGER,
		"createTime" INTEGER,
		"status" TEXT,
		"use" INTEGER,
		"wordCount" INTEGER,
		"readCount" INTEGER,
		"state" INTEGER,
		"downloadUrl" TEXT,
	PRIMARY KEY ( "id" ) 
	);