-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Dec 05, 2025 at 12:36 AM
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
-- Database: `shadowtrail`
--

-- --------------------------------------------------------

--
-- Table structure for table `levels`
--

CREATE TABLE `levels` (
  `id` int(11) NOT NULL,
  `save_id` bigint(20) NOT NULL,
  `level_number` int(11) NOT NULL,
  `current_round` int(11) DEFAULT 1,
  `temp_score` bigint(20) DEFAULT 0,
  `level_score` bigint(20) DEFAULT 0,
  `is_completed` tinyint(1) DEFAULT 0,
  `completed_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `levels`
--

INSERT INTO `levels` (`id`, `save_id`, `level_number`, `current_round`, `temp_score`, `level_score`, `is_completed`, `completed_at`) VALUES
(8, 12, 1, 1, 0, 600, 1, '2025-12-04 17:45:48'),
(9, 12, 2, 1, 0, 600, 1, '2025-12-04 17:46:21'),
(10, 12, 3, 1, 0, 535, 1, '2025-12-04 17:47:02'),
(11, 12, 4, 1, 0, 398, 1, '2025-12-04 18:05:31'),
(12, 12, 5, 1, 0, 596, 1, '2025-12-04 18:31:40'),
(18, 14, 1, 1, 0, 550, 1, '2025-12-04 23:25:16'),
(19, 14, 2, 1, 0, 575, 1, '2025-12-04 23:25:59'),
(20, 14, 3, 1, 0, 550, 1, '2025-12-04 23:27:41'),
(21, 14, 4, 1, 0, 375, 1, '2025-12-04 23:31:47'),
(22, 14, 5, 1, 0, 0, 0, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `level_configs`
--

CREATE TABLE `level_configs` (
  `level_number` int(11) NOT NULL,
  `grid_rows` int(11) NOT NULL,
  `grid_cols` int(11) NOT NULL,
  `hazard_count` int(11) NOT NULL,
  `rounds` int(11) NOT NULL DEFAULT 6
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `level_configs`
--

INSERT INTO `level_configs` (`level_number`, `grid_rows`, `grid_cols`, `hazard_count`, `rounds`) VALUES
(1, 5, 5, 4, 6),
(2, 6, 6, 6, 6),
(3, 7, 7, 8, 6),
(4, 8, 8, 10, 6),
(5, 9, 9, 12, 6);

-- --------------------------------------------------------

--
-- Table structure for table `saves`
--

CREATE TABLE `saves` (
  `id` bigint(20) NOT NULL,
  `name` varchar(120) NOT NULL,
  `unlocked_level` int(11) NOT NULL DEFAULT 1,
  `total_score` bigint(20) NOT NULL DEFAULT 0,
  `created_at` timestamp NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `saves`
--

INSERT INTO `saves` (`id`, `name`, `unlocked_level`, `total_score`, `created_at`) VALUES
(12, 'lala', 5, 2729, '2025-12-04 17:45:15'),
(14, 'lami', 5, 2050, '2025-12-04 18:47:56');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `levels`
--
ALTER TABLE `levels`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_save_level` (`save_id`,`level_number`);

--
-- Indexes for table `level_configs`
--
ALTER TABLE `level_configs`
  ADD PRIMARY KEY (`level_number`);

--
-- Indexes for table `saves`
--
ALTER TABLE `saves`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_name` (`name`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `levels`
--
ALTER TABLE `levels`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=23;

--
-- AUTO_INCREMENT for table `saves`
--
ALTER TABLE `saves`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `levels`
--
ALTER TABLE `levels`
  ADD CONSTRAINT `levels_ibfk_1` FOREIGN KEY (`save_id`) REFERENCES `saves` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
