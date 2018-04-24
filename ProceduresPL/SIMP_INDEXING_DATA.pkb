CREATE OR REPLACE PACKAGE BODY SYSIMP_UTIL.SIMP_INDEXING_DATA AS
-- VERSION HISTORY::
-- V1.0 - CREATION 2016.10 
-- V1.1 - APPLYING LOGIC TO FILTRE OUT RESERVED INDEXES FOR THE TABLES 2016.12 
-- V1.2 - ADDING LOGIC TO EXCLUDE 000 INDEXES 
-- V1.3 - Adding logic to include APM index range
	TYPE T_CHT_IDENTIFIERS IS RECORD
	(
		TABLE_NAME BW3.SYS_PROCESS_MESSAGE_PRM_LABEL.CHOICE_TABLE_NAME%TYPE,
		LOOKUP_FIELD BW3.SYS_PROCESS_MESSAGE_PRM_LABEL.CHOICE_TABLE_LOOKUP_FIELD%TYPE,
		DISPLAY_FIELD BW3.SYS_PROCESS_MESSAGE_PRM_LABEL.CHOICE_TABLE_DISPLAY_FIELD%TYPE
	);
	
	TYPE TBL_CHT_IDENTIFIERS IS TABLE OF T_CHT_IDENTIFIERS INDEX BY BW3.SYS_PROCESS_MESSAGE_PRM_LABEL.CHOICE_TABLE_NAME%TYPE;

	
	FUNCTION FETCH_CHT_VALUE_FIELDS 
	RETURN TBL_CHT_IDENTIFIERS 
	IS
	V_TBL_CHT_IDENTIFIERS TBL_CHT_IDENTIFIERS;
  
	BEGIN
		FOR x IN (	SELECT 	UPPER( CHOICE_TABLE_NAME) CHOICE_TABLE_NAME, 
							UPPER( CHOICE_TABLE_LOOKUP_FIELD) CHOICE_TABLE_LOOKUP_FIELD, 
							UPPER( CHOICE_TABLE_DISPLAY_FIELD) CHOICE_TABLE_DISPLAY_FIELD 
					FROM SYSIMP_UTIL.CHT_VALUE_TABLE)
		LOOP
		
			V_TBL_CHT_IDENTIFIERS(x.CHOICE_TABLE_NAME).TABLE_NAME   := x.CHOICE_TABLE_NAME;
			V_TBL_CHT_IDENTIFIERS(x.CHOICE_TABLE_NAME).LOOKUP_FIELD := x.CHOICE_TABLE_LOOKUP_FIELD;
			V_TBL_CHT_IDENTIFIERS(x.CHOICE_TABLE_NAME).DISPLAY_FIELD:= x.CHOICE_TABLE_DISPLAY_FIELD;
			
		END LOOP;		

		
		RETURN V_TBL_CHT_IDENTIFIERS;
	END FETCH_CHT_VALUE_FIELDS;

PROCEDURE GET_INDEX_ENVIRONMENTS( C_TAB_INDEXES OUT SYS_REFCURSOR )
IS
BEGIN
	OPEN C_TAB_INDEXES FOR
	select * from SYSIMP_UTIL.CHT_INDEXING_ENVIRONMENTS order by 2;
	
END GET_INDEX_ENVIRONMENTS;



PROCEDURE GET_INDEX_BY_TABLE( 	P_TABLE_NAME CHT_INDEXING_DATA.TABLE_NAME%TYPE,
								C_TAB_INDEXES OUT SYS_REFCURSOR )
IS
BEGIN
	OPEN C_TAB_INDEXES FOR
	SELECT A.*
	FROM (	SELECT  LISTAGG(CIE.NAME, ', ') WITHIN GROUP (ORDER BY CIE.NAME) "ENV",
					CID.INSTITUTION_NUMBER,
					CID.TABLE_NAME,
					CID.INDEX_VALUE,
					LISTAGG(CID.PROJECT, ', ') WITHIN GROUP (ORDER BY CID.ENVIRONMENT) "PROJECT",
					LISTAGG(CID.COMMENTS, ', ') WITHIN GROUP (ORDER BY CID.ENVIRONMENT) "COMMENTS",
					LISTAGG(CID.RECORD_DATE, ', ') WITHIN GROUP (ORDER BY CID.ENVIRONMENT) "RECORD_DATE",
					LISTAGG(IDS.STATUS, ', ') WITHIN GROUP (ORDER BY IDS.STATUS) "STATUS"
			FROM SYSIMP_UTIL.CHT_INDEXING_DATA CID
			INNER JOIN SYSIMP_UTIL.CHT_INDEXING_ENVIRONMENTS CIE ON CID.ENVIRONMENT = CIE.INDEX_FIELD
																	 AND CIE.SIMULATE_PROD = 1
			INNER JOIN SYSIMP_UTIL.CHT_INDEXING_STATUS IDS ON CID.STATUS = IDS.INDEX_FIELD
			WHERE UPPER(CID.TABLE_NAME) = UPPER( P_TABLE_NAME )
			GROUP BY CID.INSTITUTION_NUMBER,
					 CID.TABLE_NAME,
					 CID.INDEX_VALUE
			UNION ALL
			SELECT  LISTAGG(CIE.NAME, ', ') WITHIN GROUP (ORDER BY CIE.NAME) "ENV",
					CID.INSTITUTION_NUMBER,
					CID.TABLE_NAME,
					CID.INDEX_VALUE,
					LISTAGG(CID.PROJECT, ', ') WITHIN GROUP (ORDER BY CID.ENVIRONMENT) "PROJECT",
					LISTAGG(CID.COMMENTS, ', ') WITHIN GROUP (ORDER BY CID.ENVIRONMENT) "COMMENTS",
					LISTAGG(CID.RECORD_DATE, ', ') WITHIN GROUP (ORDER BY CID.ENVIRONMENT) "RECORD_DATE",
					LISTAGG(IDS.STATUS, ', ') WITHIN GROUP (ORDER BY IDS.STATUS) "STATUS"
			FROM SYSIMP_UTIL.CHT_INDEXING_DATA CID
			INNER JOIN SYSIMP_UTIL.CHT_INDEXING_ENVIRONMENTS CIE ON CID.ENVIRONMENT = CIE.INDEX_FIELD
																	 AND CIE.SIMULATE_PROD <> 1
			INNER JOIN SYSIMP_UTIL.CHT_INDEXING_STATUS IDS ON CID.STATUS = IDS.INDEX_FIELD
			WHERE UPPER(CID.TABLE_NAME) = UPPER( P_TABLE_NAME )
			GROUP BY CID.INSTITUTION_NUMBER,
					 CID.TABLE_NAME,
					 CID.INDEX_VALUE) A
	ORDER BY A.INDEX_VALUE;
	
END GET_INDEX_BY_TABLE;

PROCEDURE GET_NEW_INDEX_FOR_TABLE( 	P_TABLE_NAME CHT_INDEXING_DATA.TABLE_NAME%TYPE,
                                    P_OCCURANCES NUMBER,
																		P_NEW_INDEX OUT VARCHAR2,
																		P_INSTITUTION_NUMBER VARCHAR2 DEFAULT NULL,--not null = inst specific
                                    P_INDEX_START VARCHAR2 DEFAULT '001',
                                    P_INDEX_END VARCHAR2 DEFAULT '999' )
IS
    V_LENGTH NUMBER;
		V_SQL VARCHAR(2000);
		V_TRAILER VARCHAR(200) := '';
		V_INDEX_START VARCHAR(5);
BEGIN
		
		-- In Case if user provides start value as 000 owerride it ::
		IF TO_NUMBER(P_INDEX_START) = 0 THEN
			V_INDEX_START := '001';
		ELSE
			V_INDEX_START := P_INDEX_START;
		END IF;
		
		
    SELECT DATA_LENGTH
    INTO V_LENGTH
    FROM ALL_TAB_COLUMNS
    WHERE TABLE_NAME = P_TABLE_NAME
      AND COLUMN_NAME = 'INDEX_FIELD'
      AND OWNER = 'BW3';
			
			
			-- DINAMIC SQL STRING, TO ALLOW LATTER ON ASSEMBEL QUERY ON FLY
			
			
			V_SQL := 'SELECT LISTAGG(LPAD(INDEX_VALUE,'||V_LENGTH||',''0''), '','') WITHIN GROUP (ORDER BY LPAD(INDEX_VALUE,'||V_LENGTH||',''0'')) "INDEX_VALUE" '||
														'FROM (  SELECT MIN_INDEX_VALUE - 1 + LEVEL INDEX_VALUE '||
																		'FROM ( SELECT LPAD('||V_INDEX_START||','||V_LENGTH||',''0'') MIN_INDEX_VALUE, '||
																									'LPAD('||P_INDEX_END||','||V_LENGTH||',''9'') MAX_INDEX_VALUE '||
																					 'FROM DUAL '||
																				 ') '||	 
																		'CONNECT BY LEVEL <= MAX_INDEX_VALUE - MIN_INDEX_VALUE + 1 '||
																		' MINUS '||
																		'SELECT TO_NUMBER(INDEX_VALUE) '||
																		'FROM CHT_INDEXING_DATA CID2 '||
																		'WHERE CID2.TABLE_NAME = '''|| P_TABLE_NAME ||''' '||
																			'AND CID2.INDEX_VALUE BETWEEN '||V_INDEX_START||' AND '||P_INDEX_END||' '||
																			'AND CID2.INSTITUTION_NUMBER = NVL('''||P_INSTITUTION_NUMBER||''',CID2.INSTITUTION_NUMBER)) A '||
																'WHERE ROWNUM <= '||P_OCCURANCES||' ';
			

					-- CHT_ACCOUNT_CONDITION_SET do not use 100-199 or 900-999
					DBMS_OUTPUT.PUT_LINE(P_TABLE_NAME);
				IF P_TABLE_NAME = 'CHT_ACCOUNT_CONDITION_SET' THEN
				
							V_TRAILER := ' AND INDEX_VALUE not BETWEEN 100 AND 199 AND INDEX_VALUE not BETWEEN 900 AND 999';

					-- CHT_AREA_OF_ACTION	use 100 - 199 and 300-399 and 400-499
					ELSIF P_TABLE_NAME = 'CHT_AREA_OF_ACTION' THEN

									V_TRAILER := 'AND (INDEX_VALUE NOT BETWEEN 0 AND 99) AND (INDEX_VALUE NOT BETWEEN 200 AND 299) AND (INDEX_VALUE NOT BETWEEN 500 AND 999)';
					
					-- CHT_CARD_BRAND [201 to 220] adding for APM [221 and 320 ]
					ELSIF P_TABLE_NAME = 'CHT_CARD_BRAND' THEN
					
								V_TRAILER := ' AND ((INDEX_VALUE BETWEEN 201 AND 220) OR (INDEX_VALUE BETWEEN 221 AND 320)) ';
					
					-- CHT_COUNTRY_STATE 200 - 350
					ELSIF P_TABLE_NAME = 'CHT_COUNTRY_STATE' THEN
					
							V_TRAILER := ' AND INDEX_VALUE BETWEEN 200 AND 350';

					-- CHT_FEE_IDENTIFIER 2000-2999
					ELSIF P_TABLE_NAME = 'CHT_FEE_IDENTIFIER' THEN
					
											V_TRAILER := ' AND INDEX_VALUE BETWEEN 2000 AND 2999';
					
					-- CHT_ISO_BUSS_CLASS 921-931 are not in line with the RS2 BWT Update doc
					ELSIF P_TABLE_NAME = 'CHT_ISO_BUSS_CLASS' THEN
									
									V_TRAILER := ' AND INDEX_VALUE BETWEEN 920 AND 931';
					
					-- CHT_REGION BIN Over Ride: 800 to 899, reserved range 900 to 998
					ELSIF P_TABLE_NAME = 'CHT_REGION' THEN
					
									V_TRAILER := ' AND INDEX_VALUE NOT BETWEEN 800 AND 998';	
					
					-- CHT_SERVICES use 0-700
					ELSIF P_TABLE_NAME = 'CHT_SERVICES' THEN
				
							V_TRAILER := ' AND INDEX_VALUE BETWEEN 0 AND 700';

					-- DEFAULT CASE, when all other CHT tables
					ELSE
								V_TRAILER := '';
				
				END IF; -- finishing if block

				
				--combining and excecuting
				
				V_SQL := V_SQL || V_TRAILER;
                
				EXECUTE IMMEDIATE V_SQL INTO P_NEW_INDEX;
				
				
				
				
				
				
        
END GET_NEW_INDEX_FOR_TABLE;	  

FUNCTION INSERT_NEW_INDEX(  P_ENVIRONMENT CHT_INDEXING_DATA.ENVIRONMENT%TYPE,
                            P_INSTITUTION_NUMBER CHT_INDEXING_DATA.INSTITUTION_NUMBER%TYPE,
                            P_TABLE_NAME CHT_INDEXING_DATA.TABLE_NAME%TYPE,
                            P_INDEX_VALUE CHT_INDEXING_DATA.INDEX_VALUE%TYPE,
                            P_PROJECT CHT_INDEXING_DATA.PROJECT%TYPE,
                            P_COMMENTS CHT_INDEXING_DATA.COMMENTS%TYPE,
							P_STATUS CHT_INDEXING_DATA.STATUS%TYPE DEFAULT '001')--PENDING
RETURN NUMBER                            
IS
    V_SUCCESS NUMBER := 0;
    V_USERNAME CHT_INDEXING_DATA.USERNAME%TYPE;
    V_ENVIRONMENT CHT_INDEXING_DATA.ENVIRONMENT%TYPE;
BEGIN

    BEGIN
    
    SELECT USER
    INTO V_USERNAME
    FROM DUAL;
    
    SELECT INDEX_FIELD 
    INTO V_ENVIRONMENT
    FROM SYSIMP_UTIL.CHT_INDEXING_ENVIRONMENTS CIE
    WHERE CIE.NAME = P_ENVIRONMENT;

    INSERT INTO SYSIMP_UTIL.CHT_INDEXING_DATA VALUES
    ( V_ENVIRONMENT, P_INSTITUTION_NUMBER, P_TABLE_NAME,
      P_INDEX_VALUE, P_PROJECT, P_COMMENTS,
      V_USERNAME, TO_CHAR( SYSDATE, 'YYYYMMDD'), P_STATUS );
       
    V_SUCCESS := 1;
      
    EXCEPTION WHEN OTHERS THEN
        V_SUCCESS := 0;  
    END;
    
    RETURN V_SUCCESS;
	  
END INSERT_NEW_INDEX;	

FUNCTION DELETE_INDEX(  P_ENVIRONMENT CHT_INDEXING_DATA.ENVIRONMENT%TYPE,
                        P_INSTITUTION_NUMBER CHT_INDEXING_DATA.INSTITUTION_NUMBER%TYPE,
                        P_TABLE_NAME CHT_INDEXING_DATA.TABLE_NAME%TYPE,
                        P_INDEX_VALUE CHT_INDEXING_DATA.INDEX_VALUE%TYPE)
RETURN NUMBER                        
IS
    V_SUCCESS NUMBER := 0;
    V_USERNAME CHT_INDEXING_DATA.USERNAME%TYPE;
    V_ROW_ENVIRONMENT SYSIMP_UTIL.CHT_INDEXING_ENVIRONMENTS%ROWTYPE;
BEGIN
    SELECT * 
    INTO V_ROW_ENVIRONMENT
    FROM SYSIMP_UTIL.CHT_INDEXING_ENVIRONMENTS CIE
    WHERE CIE.NAME = P_ENVIRONMENT;

    IF V_ROW_ENVIRONMENT.SIMULATE_PROD <> 1
    THEN
        BEGIN
        
        DELETE FROM SYSIMP_UTIL.CHT_INDEXING_DATA
        WHERE ENVIRONMENT = V_ROW_ENVIRONMENT.INDEX_FIELD
          AND INSTITUTION_NUMBER = P_INSTITUTION_NUMBER
          AND TABLE_NAME = P_TABLE_NAME
          AND INDEX_VALUE = P_INDEX_VALUE;
        
        V_SUCCESS := 1;
        
        EXCEPTION WHEN OTHERS THEN
            V_SUCCESS := 0;

        END;
    END IF;
	  
    RETURN V_SUCCESS;
END DELETE_INDEX;  
	  
FUNCTION SYNCH_ENVIRONMENT_INDEXES
RETURN NUMBER
IS
    V_RESPONSE NUMBER:=0;
    V_LINK_NAME VARCHAR2(20):= 'CHT_INDEXES_LINK';
    V_USERNAME VARCHAR2(35);
    V_IMPORT_SQL VARCHAR2(2000);
    V_PROD_ENVIRONMENT VARCHAR2(50);
    V_STATUS VARCHAR2(3) := '001';
    V_CURRENT_ENV VARCHAR2(30);
    
    FUNCTION CREATE_DB_LINK( P_DB_LINK_NAME VARCHAR2,
                              P_DB_NAMESPACE VARCHAR2 )  
    RETURN BOOLEAN
    AS
        V_SQL VARCHAR2(2000);
        V_COUNT NUMBER;
        V_CREATION_SUCCESS BOOLEAN:= FALSE;
    BEGIN
        IF P_DB_LINK_NAME IS NOT NULL
        THEN
            BEGIN
                V_SQL   := 'ALTER SESSION CLOSE DATABASE LINK ' || P_DB_LINK_NAME;
                EXECUTE IMMEDIATE (V_SQL);
            EXCEPTION WHEN OTHERS THEN
                NULL;
            END;
            
            V_SQL   := 'CREATE DATABASE LINK ' || P_DB_LINK_NAME ||' CONNECT TO DBA_JOBS_TEST IDENTIFIED BY "carlow" USING ''' || P_DB_NAMESPACE || '''';

            EXECUTE IMMEDIATE(V_SQL);
            
            IF P_DB_NAMESPACE != 'BW3_SC'
            THEN          
                V_SQL   := 'SELECT COUNT(1) FROM DUAL@' || P_DB_LINK_NAME;
                
								Begin
								EXECUTE IMMEDIATE(V_SQL) INTO V_COUNT;
								
																											EXCEPTION
								WHEN no_data_found THEN
									
								DBMS_OUTPUT.PUT_LINE('noRecords in the table');
								DBMS_OUTPUT.PUT_LINE (SQLERRM);
								NULL;
								end;

            END IF;
						

                     
            IF V_COUNT = 1
            THEN
                V_CREATION_SUCCESS := TRUE;
								DBMS_OUTPUT.PUT_LINE('Link Created succesfully for: '|| P_DB_NAMESPACE);
						ELSE
								DBMS_OUTPUT.PUT_LINE('Failed to create Link: '|| P_DB_NAMESPACE);
            END IF;
        
        END IF;
        
        RETURN V_CREATION_SUCCESS;
              
        EXCEPTION
        WHEN OTHERS THEN
            V_CREATION_SUCCESS := FALSE;
            
        RETURN V_CREATION_SUCCESS;
    END CREATE_DB_LINK;
       
    PROCEDURE DROP_DB_LINK ( P_DB_LINK_NAME VARCHAR2 )
    IS
        V_COUNT NUMBER :=0;
    BEGIN
        IF P_DB_LINK_NAME IS NOT NULL
        THEN
            EXECUTE IMMEDIATE ('SELECT COUNT(1) FROM ALL_DB_LINKS WHERE DB_LINK  = ''' || P_DB_LINK_NAME || '''') INTO V_COUNT;

            IF V_COUNT = 1
            THEN
                EXECUTE IMMEDIATE( 'DROP DATABASE LINK ' || P_DB_LINK_NAME ) ;
								DBMS_OUTPUT.PUT_LINE('Dropping DB Link');
            END IF;
        END IF;
    END DROP_DB_LINK;  
    
    PROCEDURE IMPORT_TABLES (   P_CURR_ENVIRONMENT VARCHAR2,
                                P_USERNAME VARCHAR2,
                                P_STATUS VARCHAR,
                                P_LINK_NAME VARCHAR,
                                P_PROD_ENVIRONMENT VARCHAR )
                    
    IS
        V_LINK_NAME VARCHAR2( 25 ) := P_LINK_NAME;
        V_CHT_IDENTIFIERS TBL_CHT_IDENTIFIERS;        
    BEGIN
    	V_CHT_IDENTIFIERS := FETCH_CHT_VALUE_FIELDS;
        IF LENGTH(TRIM(V_LINK_NAME)) >0
        THEN
            V_LINK_NAME := '@' || V_LINK_NAME;
        END IF;
        
        FOR C_TABLE_NAMES IN ( -- select choice_table_name as TABLE_NAME from sysimp_util.cht_value_table
       /*
                                SELECT TABLE_NAME
                                FROM ALL_TABLES AT
                                WHERE AT.OWNER = 'BW3'
                                  AND AT.TABLE_NAME LIKE 'CHT%'
                                  AND TABLE_NAME NOT IN ('CHT_CHARGEBACK_REASON',
                                                         'CHT_FRAUD_RESPONSE_CODE',
                                                         'CHT_CURRENCY',
                                                         'CHT_VISA_SOURCE_BIN',
																												 --new entries here
																												 'CHT_ACCOUNT_TRANSFER_ID',
																												 'CHT_TRANSFER_CATEGORY',
																												 'CHT_TRANSFER_METHOD',
																												 'CHT_FEE_DESTINATION',
                                                                                                                 'CHT_VCR_DISPUTE_STATUS',
                                                                                                                 'CHT_CHECKSUM',
                                                                                                                 'CHT_CHARACTER_ENCODING',
                                                                                                                 'CHT_TAX_TARIFF',
                                                                                                                 'CHT_MUNICIPAL_DEPARTMENT',
                                                                                                                 'CHT_MUNICIPAL_CODE')
                                                                                                                 */
                                                                                                                 
                     SELECT distinct CHOICE_TABLE_NAME as TABLE_NAME FROM SYSIMP_UTIL.CHT_VALUE_TABLE where CHOICE_TABLE_NAME NOT IN (
'CHT_CHARGEBACK_REASON',
'CHT_CURRENCY',
'CHT_FRAUD_RESPONSE_CODE',
'CHT_VISA_SOURCE_BIN'
)                                                                                           
                                                                                                                 
                                                                                                                 )
        LOOP
				--DBMS_OUTPUT.PUT_LINE('Debug: Current env'|| P_CURR_ENVIRONMENT);
				--DBMS_OUTPUT.PUT_LINE('Debug: Current table'|| C_TABLE_NAMES.TABLE_NAME);
				dbms_output.put_line('Debug here table: '|| C_TABLE_NAMES.TABLE_NAME);
-- Dont import if it exsists in prod or in current environment       

            V_IMPORT_SQL := 'INSERT INTO SYSIMP_UTIL.CHT_INDEXING_DATA ' ||
                            'SELECT ''' || P_CURR_ENVIRONMENT || ''' ENVIRONMENT, ' ||
                                    ' CTT.INSTITUTION_NUMBER, ''' ||
                                    C_TABLE_NAMES.TABLE_NAME || ''' TABLE_NAME, '  ||
                                    ' CTT.INDEX_FIELD, ' ||
                                    '''Import'' PROJECT_NAME, ' ||
                                    'CTT.' || V_CHT_IDENTIFIERS(C_TABLE_NAMES.TABLE_NAME).DISPLAY_FIELD || ' COMMENTS, ''' ||
                                    P_USERNAME || ''' USERNAME ' || ', '''  ||
                                    TO_CHAR( SYSDATE, 'YYYYMMDD') ||''', ''' ||
                                    P_STATUS || ''' STATUS ' || -- CONFIRMED
                            'FROM BW3.' || C_TABLE_NAMES.TABLE_NAME || V_LINK_NAME || ' CTT ' ||
                            'WHERE CTT.LANGUAGE = ''USA'' ' ||
                              'AND NOT EXISTS (  SELECT 1 ' ||
                                                'FROM CHT_INDEXING_DATA CID ' ||
                                                'WHERE CID.ENVIRONMENT IN (''' || P_PROD_ENVIRONMENT || ''',''' || P_CURR_ENVIRONMENT || ''')' ||
                                                  'AND CID.INSTITUTION_NUMBER = CTT.INSTITUTION_NUMBER ' ||
                                                  'AND CID.TABLE_NAME = ''' || C_TABLE_NAMES.TABLE_NAME || ''' ' ||
                                                  'AND CID.INDEX_VALUE = CTT.INDEX_FIELD)';                                                
					BEGIN
            EXECUTE IMMEDIATE (V_IMPORT_SQL);
						
					EXCEPTION
								WHEN no_data_found THEN
									
                                    DBMS_OUTPUT.PUT_LINE('noRecords in the table');
                                    DBMS_OUTPUT.PUT_LINE (SQLERRM);
                                WHEN OTHERS THEN
                                    DBMS_OUTPUT.PUT_LINE('Other exception... Most likley table does not exist in this test system');
						
					END;
            --DBMS_OUTPUT.PUT_LINE ( V_IMPORT_SQL);
						
        END LOOP;
				/*
				EXCEPTION
					WHEN no_data_found THEN
					
					DBMS_OUTPUT.PUT_LINE('noRec');
					DBMS_OUTPUT.PUT_LINE (SQLERRM);
					
				WHEN OTHERS THEN
					DBMS_OUTPUT.PUT_LINE('error');
					DBMS_OUTPUT.PUT_LINE (SQLERRM);
				*/
				
				
    END IMPORT_TABLES; 

BEGIN
	BEGIN
		
        --GET THE USERNAME
        SELECT USER
        INTO V_USERNAME
        FROM DUAL;
        
        --GET PRODUCTION ENVIRONMENT
        SELECT INDEX_FIELD
        INTO V_PROD_ENVIRONMENT
        FROM CHT_INDEXING_ENVIRONMENTS CIE
        WHERE CIE.SIMULATE_PROD = '1';
        
        SELECT SYS_CONTEXT('userenv','instance_name')
        INTO V_CURRENT_ENV
        FROM DUAL;
        
        FOR C_ENVIRONMENTS IN ( SELECT *
                                FROM SYSIMP_UTIL.CHT_INDEXING_ENVIRONMENTS CIE
                                ORDER BY SIMULATE_PROD DESC, INDEX_FIELD)
        LOOP
        
            V_STATUS :='001';-- BOOKED
            
            -- IF PROD, DO THE DELETE ( PROD SHOULD ALWAYS BE FULLY REFRESHED
            IF C_ENVIRONMENTS.SIMULATE_PROD = 1
            THEN
                DELETE FROM SYSIMP_UTIL.CHT_INDEXING_DATA
                WHERE ENVIRONMENT = C_ENVIRONMENTS.INDEX_FIELD;
                
                V_STATUS :='002'; -- CONFIRMED
            END IF;
            
            --IF THE CURRENT ENVIRONMENT IS THE SAME LIKE THE ONE WE ARE LOOPING IN, NO DB LINK IS NEEDED
            IF V_CURRENT_ENV = C_ENVIRONMENTS.NAME
            THEN
                V_LINK_NAME := NULL;
            ELSE
                V_LINK_NAME := 'CHT_INDEXES_LINK';
            END IF;         
            IF CREATE_DB_LINK ( V_LINK_NAME,
                                C_ENVIRONMENTS.NAME ) 
            THEN        
         
                IMPORT_TABLES(  C_ENVIRONMENTS.INDEX_FIELD,
                                V_USERNAME,
                                V_STATUS,
                                V_LINK_NAME,
                                V_PROD_ENVIRONMENT );
                
						ELSE
						DBMS_OUTPUT.PUT_LINE('There was a prpblem with DB_LINK CREATION');
            END IF;
            
            DROP_DB_LINK ( V_LINK_NAME );
            
        END LOOP;
        
        /*REMOVE RECORDS IN TEST SYSTEMS THAT EXISTS IN PRODUCTION
        UPDATE THE PROJECT / COMMENTS FROM EXISTENT TEST SYSTEMS*/
        FOR x in (  SELECT CID_PROD.*
                    FROM CHT_INDEXING_DATA CID_PROD
                    INNER JOIN CHT_INDEXING_ENVIRONMENTS CIE ON CID_PROD.ENVIRONMENT = CIE.INDEX_FIELD
                                                            AND CIE.SIMULATE_PROD <> '1'
                      and exists(SELECT 1
                                FROM CHT_INDEXING_DATA CID
                                INNER JOIN CHT_INDEXING_ENVIRONMENTS CIE ON CID.ENVIRONMENT = CIE.INDEX_FIELD
                                                                        AND CIE.SIMULATE_PROD = '1'
                                WHERE CID.TABLE_NAME = CID_PROD.TABLE_NAME
                                  AND CID.INSTITUTION_NUMBER = CID_PROD.INSTITUTION_NUMBER
                                  AND CID.INDEX_VALUE = CID_PROD.INDEX_VALUE))
        LOOP
     
            UPDATE SYSIMP_UTIL.CHT_INDEXING_DATA
            SET PROJECT = x.project,
                COMMENTS = x.comments,
                USERNAME = x.username
            WHERE ENVIRONMENT           = V_PROD_ENVIRONMENT
              AND INSTITUTION_NUMBER    = x.INSTITUTION_NUMBER
              AND TABLE_NAME            = x.TABLE_NAME
              AND INDEX_VALUE           = x.INDEX_VALUE;
              
            DELETE FROM SYSIMP_UTIL.CHT_INDEXING_DATA
            WHERE ENVIRONMENT           <> V_PROD_ENVIRONMENT
              AND INSTITUTION_NUMBER    = x.INSTITUTION_NUMBER
              AND TABLE_NAME            = x.TABLE_NAME
              AND INDEX_VALUE           = x.INDEX_VALUE;
          
        END LOOP;
        
        COMMIT;
        V_RESPONSE := 1;
        EXCEPTION WHEN OTHERS THEN
            BEGIN
                DROP_DB_LINK ( V_LINK_NAME );
                dbms_output.put_line (sqlerrm);
                -- EXCEPTION WHEN OTHERS THEN null ;
            END;
    END;    
    RETURN V_RESPONSE;
END SYNCH_ENVIRONMENT_INDEXES;

FUNCTION GENERATE_INSERT_SCRIPT ( 	P_INSTITUTION_NUMBER VARCHAR2,
									P_TABLE_NAME VARCHAR2,
									P_NEW_INDEX VARCHAR2,
									P_NEW_VALUE VARCHAR2 )
RETURN VARCHAR									
IS
	V_COLUMN_DESC VARCHAR2(1000);
	V_INSERT_SQL VARCHAR2(2000);
	V_CURRENT_VALUE VARCHAR2(1000);
	V_CHT_IDENTIFIERS TBL_CHT_IDENTIFIERS;
	  
	FUNCTION GET_MOST_USED_VALUE (	P_INST_NUMBER VARCHAR2,
									P_TBL_NAME VARCHAR2,
									P_COLUMN_NAME VARCHAR2 )
	RETURN VARCHAR2
	IS
		V_VALUE VARCHAR2(100);
		V_SQL VARCHAR2 ( 1000);
	BEGIN
	
		IF P_COLUMN_NAME = 'INSTITUTION_NUMBER' THEN
		V_VALUE := P_INST_NUMBER;
		ELSE
				V_SQL := 	'SELECT ' || P_COLUMN_NAME ||
							' FROM (  SELECT ' || P_COLUMN_NAME || ', COUNT(*) ' ||
									' FROM BW3.' || P_TBL_NAME ||
									' WHERE INSTITUTION_NUMBER = ''00000000''' ||
										' AND LANGUAGE = ''USA''' ||
									' GROUP BY ' || P_COLUMN_NAME ||
									' ORDER BY 2 DESC )' ||
							' WHERE ROWNUM <2';
							EXECUTE IMMEDIATE V_SQL INTO V_VALUE;
			END IF;

		RETURN V_VALUE;
	END GET_MOST_USED_VALUE;
BEGIN

	V_CHT_IDENTIFIERS := FETCH_CHT_VALUE_FIELDS;
	
	SELECT LISTAGG(COLUMN_NAME, ', ') WITHIN GROUP (ORDER BY COLUMN_ID) "COLUMNS"
	INTO V_COLUMN_DESC
	FROM ALL_TAB_COLUMNS
	WHERE TABLE_NAME = P_TABLE_NAME
	ORDER BY COLUMN_ID ASC;

	V_INSERT_SQL:= 'INSERT INTO BW3.' || P_TABLE_NAME || '(';
	V_INSERT_SQL:= V_INSERT_SQL || V_COLUMN_DESC || ') VALUES (';
	
	FOR x IN (  SELECT COLUMN_NAME
				FROM ALL_TAB_COLUMNS
				WHERE TABLE_NAME = P_TABLE_NAME
				ORDER BY COLUMN_ID ASC)
	LOOP
		IF X.COLUMN_NAME = V_CHT_IDENTIFIERS ( P_TABLE_NAME ).LOOKUP_FIELD
		THEN
			V_CURRENT_VALUE := P_NEW_INDEX;
		ELSIF X.COLUMN_NAME = V_CHT_IDENTIFIERS ( P_TABLE_NAME ).DISPLAY_FIELD
		THEN
			V_CURRENT_VALUE := P_NEW_VALUE;
		ELSE
			V_CURRENT_VALUE := GET_MOST_USED_VALUE (P_INSTITUTION_NUMBER,
													P_TABLE_NAME,
													X.COLUMN_NAME );
		END IF;
			
		V_INSERT_SQL:= V_INSERT_SQL || '''' || V_CURRENT_VALUE || ''',';
	END LOOP;
	--REMOVE THE LAST ,
	V_INSERT_SQL := SUBSTR(V_INSERT_SQL,0,LENGTH( V_INSERT_SQL ) -1 );
	
	V_INSERT_SQL:= V_INSERT_SQL || ');';
	
	
	-- adding extra german language insert sent to the fron end::
	
	--REPLACE('0000123', '0', ' ');
	V_INSERT_SQL := V_INSERT_SQL || CHR(13) || CHR(10) || REPLACE(V_INSERT_SQL, '''USA''','''GER''');
	
	
	
	
	
	
	RETURN V_INSERT_SQL;
END GENERATE_INSERT_SCRIPT;									

END SIMP_INDEXING_DATA;
/