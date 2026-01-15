-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jan 15, 2026 at 07:23 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `business_center`
--

-- --------------------------------------------------------

--
-- Table structure for table `korisnik`
--

CREATE TABLE `korisnik` (
  `id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `ime` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `novac` decimal(10,2) DEFAULT 1000.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

--
-- Dumping data for table `korisnik`
--

INSERT INTO `korisnik` (`id`, `username`, `password`, `ime`, `email`, `novac`) VALUES
(1, 'admin', 'admin123', 'Administrator', 'admin@businesscenter.com', 5000.00),
(2, 'korisnik1', 'korisnik123', 'Test Korisnik', 'korisnik1@businesscenter.com', 1000.00),
(3, 'stefj', '$2a$10$3CI7wimnC7csSQAiJfC8l.tpDG1Cv6nEmlyxe1I0bsfAoZVinv356', 'Jovana', 'stefj@example.com', 1000.00),
(4, 'stefj1', '$2a$10$ea6ZqGd5MGOehdxakpPuCu1HeJ3pyehHiNtKlvVsWRI/PraJaMJxS', 'Jovana1', 'stefj1@example.com', 950.00),
(5, 'stefj12', '$2a$10$LynFrKyBlFGngccbKFQepu6EPsNgPgI30Tz0VXzhEK.8sAWZ7Xkf6', 'Jovana12', 'stefj12@example.com', 600.00);

-- --------------------------------------------------------

--
-- Table structure for table `resurs`
--

CREATE TABLE `resurs` (
  `id` int(11) NOT NULL,
  `naziv` varchar(100) NOT NULL,
  `tip` varchar(50) NOT NULL,
  `radno_vreme_pocetak` time NOT NULL DEFAULT '08:00:00',
  `radno_vreme_kraj` time NOT NULL DEFAULT '20:00:00',
  `cena_po_terminu` decimal(10,2) DEFAULT 100.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

--
-- Dumping data for table `resurs`
--

INSERT INTO `resurs` (`id`, `naziv`, `tip`, `radno_vreme_pocetak`, `radno_vreme_kraj`, `cena_po_terminu`) VALUES
(1, 'Konferencijska sala A', 'PROSTORIJA', '08:00:00', '20:00:00', 50.00),
(2, 'Projektor', 'OPREMA', '09:00:00', '18:00:00', 15.00),
(3, 'Sala za sastanke', 'PROSTORIJA', '08:00:00', '22:00:00', 30.00);

-- --------------------------------------------------------

--
-- Table structure for table `rezervacija`
--

CREATE TABLE `rezervacija` (
  `id` int(11) NOT NULL,
  `korisnik_id` int(11) NOT NULL,
  `resurs_id` int(11) NOT NULL,
  `datum_pocetka` datetime NOT NULL,
  `datum_kraja` datetime NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'AKTIVNA' COMMENT 'AKTIVNA, OBRISANA, ZAVRSENA',
  `cena_transakcije` decimal(10,2) NOT NULL,
  `serija_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

--
-- Dumping data for table `rezervacija`
--

INSERT INTO `rezervacija` (`id`, `korisnik_id`, `resurs_id`, `datum_pocetka`, `datum_kraja`, `status`, `cena_transakcije`, `serija_id`) VALUES
(1, 4, 1, '2026-01-29 11:00:00', '2026-01-29 13:00:00', 'AKTIVNA', 0.00, NULL),
(2, 5, 1, '2026-01-30 11:00:00', '2026-01-30 13:00:00', 'AKTIVNA', 50.00, NULL),
(3, 5, 1, '2026-01-30 14:00:00', '2026-01-30 14:30:00', 'OBRISANA', 50.00, NULL),
(4, 5, 1, '2026-03-01 11:00:00', '2026-03-01 13:00:00', 'OBRISANA', 0.00, 1),
(5, 5, 1, '2026-03-08 11:00:00', '2026-03-08 13:00:00', 'OBRISANA', 0.00, 1),
(6, 5, 1, '2026-03-15 11:00:00', '2026-03-15 13:00:00', 'OBRISANA', 0.00, 1),
(7, 5, 1, '2026-03-22 11:00:00', '2026-03-22 13:00:00', 'OBRISANA', 0.00, 1),
(8, 5, 1, '2026-03-01 11:00:00', '2026-03-01 13:00:00', 'OBRISANA', 50.00, 2),
(9, 5, 1, '2026-03-08 11:00:00', '2026-03-08 13:00:00', 'AKTIVNA', 50.00, 2),
(10, 5, 1, '2026-03-15 11:00:00', '2026-03-15 13:00:00', 'AKTIVNA', 50.00, 2),
(11, 5, 1, '2026-03-22 11:00:00', '2026-03-22 13:00:00', 'AKTIVNA', 50.00, 2);

-- --------------------------------------------------------

--
-- Table structure for table `rezervaciona_serija`
--

CREATE TABLE `rezervaciona_serija` (
  `id` int(11) NOT NULL,
  `korisnik_id` int(11) NOT NULL,
  `resurs_id` int(11) NOT NULL,
  `frekvencija` varchar(20) NOT NULL COMMENT 'DNEVNO, RADNI_DANI, NEDELJNO, MESEČNO',
  `datum_pocetka` datetime NOT NULL,
  `datum_kraja` datetime NOT NULL,
  `ukupna_cena` decimal(10,2) NOT NULL,
  `status` varchar(20) DEFAULT 'AKTIVNA'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

--
-- Dumping data for table `rezervaciona_serija`
--

INSERT INTO `rezervaciona_serija` (`id`, `korisnik_id`, `resurs_id`, `frekvencija`, `datum_pocetka`, `datum_kraja`, `ukupna_cena`, `status`) VALUES
(1, 5, 1, 'NEDELJNO', '2026-03-01 11:00:00', '2026-03-23 00:59:59', 0.00, 'AKTIVNA'),
(2, 5, 1, 'NEDELJNO', '2026-03-01 11:00:00', '2026-03-23 00:59:59', 200.00, 'AKTIVNA');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `korisnik`
--
ALTER TABLE `korisnik`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username_UNIQUE` (`username`);

--
-- Indexes for table `resurs`
--
ALTER TABLE `resurs`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `rezervacija`
--
ALTER TABLE `rezervacija`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_rezervacija_korisnik_idx` (`korisnik_id`),
  ADD KEY `fk_rezervacija_resurs_idx` (`resurs_id`),
  ADD KEY `fk_rezervacija_serija_idx` (`serija_id`),
  ADD KEY `idx_rezervacija_datum` (`datum_pocetka`,`datum_kraja`),
  ADD KEY `idx_rezervacija_status` (`status`),
  ADD KEY `idx_rezervacija_korisnik` (`korisnik_id`),
  ADD KEY `idx_rezervacija_resurs` (`resurs_id`);

--
-- Indexes for table `rezervaciona_serija`
--
ALTER TABLE `rezervaciona_serija`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_rezervaciona_serija_korisnik_idx` (`korisnik_id`),
  ADD KEY `fk_rezervaciona_serija_resurs_idx` (`resurs_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `korisnik`
--
ALTER TABLE `korisnik`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `resurs`
--
ALTER TABLE `resurs`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `rezervacija`
--
ALTER TABLE `rezervacija`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `rezervaciona_serija`
--
ALTER TABLE `rezervaciona_serija`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `rezervacija`
--
ALTER TABLE `rezervacija`
  ADD CONSTRAINT `fk_rezervacija_korisnik` FOREIGN KEY (`korisnik_id`) REFERENCES `korisnik` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_rezervacija_resurs` FOREIGN KEY (`resurs_id`) REFERENCES `resurs` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_rezervacija_serija` FOREIGN KEY (`serija_id`) REFERENCES `rezervaciona_serija` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `rezervaciona_serija`
--
ALTER TABLE `rezervaciona_serija`
  ADD CONSTRAINT `fk_rezervaciona_serija_korisnik` FOREIGN KEY (`korisnik_id`) REFERENCES `korisnik` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_rezervaciona_serija_resurs` FOREIGN KEY (`resurs_id`) REFERENCES `resurs` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
