-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: localhost    Database: form_management
-- ------------------------------------------------------
-- Server version	8.4.4

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `form_fields`
--

DROP TABLE IF EXISTS `form_fields`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `form_fields` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `display_order` int NOT NULL,
  `label` varchar(255) NOT NULL,
  `options_json` text,
  `required` bit(1) NOT NULL,
  `type` enum('COLOR','DATE','NUMBER','SELECT','TEXT') NOT NULL,
  `form_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `form_id` (`form_id`,`display_order`),
  CONSTRAINT `FKoaf23i4o45w65iclgspjv8mg0` FOREIGN KEY (`form_id`) REFERENCES `forms` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `form_fields`
--

LOCK TABLES `form_fields` WRITE;
/*!40000 ALTER TABLE `form_fields` DISABLE KEYS */;
INSERT INTO `form_fields` VALUES (1,'2026-05-03 12:23:21.947642','2026-05-03 12:23:21.947642',1,'Full Name',NULL,_binary '','TEXT',1),(2,'2026-05-03 12:23:21.954932','2026-05-03 12:23:21.954932',2,'Age',NULL,_binary '\0','NUMBER',1),(3,'2026-05-03 12:23:21.958934','2026-05-03 12:23:21.958934',3,'Favorite Color',NULL,_binary '\0','COLOR',1),(4,'2026-05-03 12:23:49.531040','2026-05-03 12:23:49.531040',4,'Country','[\"US\", \"CA\", \"VN\"]',_binary '\0','SELECT',1),(5,'2026-05-03 12:38:05.738800','2026-05-03 12:38:17.932630',5,'Country of Residence',NULL,_binary '','TEXT',1);
/*!40000 ALTER TABLE `form_fields` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `forms`
--

DROP TABLE IF EXISTS `forms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `forms` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `description` text,
  `display_order` int NOT NULL,
  `status` enum('ACTIVE','DRAFT') NOT NULL,
  `title` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `display_order` (`display_order`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forms`
--

LOCK TABLES `forms` WRITE;
/*!40000 ALTER TABLE `forms` DISABLE KEYS */;
INSERT INTO `forms` VALUES (1,'2026-05-03 12:23:21.908583','2026-05-03 12:32:56.541369','Form for employee updated',1,'ACTIVE','Form for employee');
/*!40000 ALTER TABLE `forms` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `submission_values`
--

DROP TABLE IF EXISTS `submission_values`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `submission_values` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `value` text NOT NULL,
  `field_id` bigint NOT NULL,
  `submission_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKq0y0ffeo2lyy3h99xofe440bl` (`field_id`),
  KEY `FK51i4f1ib29mi3ps16fuc3qc80` (`submission_id`),
  CONSTRAINT `FK51i4f1ib29mi3ps16fuc3qc80` FOREIGN KEY (`submission_id`) REFERENCES `submissions` (`id`),
  CONSTRAINT `FKq0y0ffeo2lyy3h99xofe440bl` FOREIGN KEY (`field_id`) REFERENCES `form_fields` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `submission_values`
--

LOCK TABLES `submission_values` WRITE;
/*!40000 ALTER TABLE `submission_values` DISABLE KEYS */;
INSERT INTO `submission_values` VALUES (1,'Nguyễn Văn A',1,1),(2,'30',2,1),(3,'#00A1FF',3,1),(4,'VN',4,1),(5,'Hà Nội',5,1);
/*!40000 ALTER TABLE `submission_values` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `submissions`
--

DROP TABLE IF EXISTS `submissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `submissions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `form_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKsa451myl41cavhch5eir0unua` (`form_id`),
  CONSTRAINT `FKsa451myl41cavhch5eir0unua` FOREIGN KEY (`form_id`) REFERENCES `forms` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `submissions`
--

LOCK TABLES `submissions` WRITE;
/*!40000 ALTER TABLE `submissions` DISABLE KEYS */;
INSERT INTO `submissions` VALUES (1,'2026-05-03 12:42:57.721747',1);
/*!40000 ALTER TABLE `submissions` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-04  1:00:09
