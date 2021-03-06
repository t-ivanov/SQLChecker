﻿/*1a*/
# Hier steht mein Kommentar für die Musterlösung und Aufgabe 1a
SELECT 
    c.firstname,
    c.lastname,
    r.Matrikelnummer,
    SUM(r.NoReservedSeats)
FROM # 1a # Kommentar
    reservation r,
    customer c
WHERE
    r.matrikelnummer = c.matrikelnummer
GROUP BY r.Matrikelnummer
HAVING SUM(r.NoReservedSeats) = 4;
-- Kommentar für die Musterlösung
/*1b*/
-- Kommentar 1b
SELECT 
    COUNT(DISTINCT (r.Matrikelnummer))
FROM
    reservation r,
    customer cu,
    address a,
    country co
WHERE
    r.Matrikelnummer = cu.Matrikelnummer
        AND cu.AddressID = a.AddressID
        AND a.town LIKE 'Berlin';
-- Kommentar für die Musterlösung 1b
/*1c*/
/* Kommentar für die Musterlösung */
SELECT 
    sum(`r`.`NoReservedSeats`) AS `NoReservedSeats`
FROM
    (`flightexecution` `f`
    JOIN `reservation` `r`)
WHERE
    ((`f`.`ICAO_Code_Origin` = 'EDDF')
        AND (`f`.`ICAO_Code_Destination` = 'CYYZ')
        AND (`f`.`FlightNo` = `r`.`FlightNo`)
        AND (`f`.`DepartureDateAndTimeUTC` > NOW())
        AND (`f`.`DepartureDateAndTimeUTC` < (NOW() + INTERVAL 65 DAY)));

/*1d*/
/* Kommentar für die Musterlösung */
SELECT 
	DISTINCT r.FlightNo,
	(SELECT SUM(rv.NoReservedSeats) 
		FROM reservation rv
		WHERE rv.FlightNo = r.FlightNo)
FROM reservation r;
