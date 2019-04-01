DROP TABLE IF EXISTS soldproducts, offers, products, categories, maincategories, 
users; 

CREATE TABLE users ( 
     userid      INT auto_increment, 
     username    VARCHAR(32) NOT NULL, 
     password    VARCHAR(32) NOT NULL, 
     firstname   VARCHAR(20) NOT NULL, 
     lastname    VARCHAR(20), 
     phonenumber VARCHAR(12), 
     PRIMARY KEY (userid), 
     UNIQUE (username) 
  ); 

CREATE TABLE maincategories ( 
     maincatname VARCHAR(40) NOT NULL, 
     PRIMARY KEY (maincatname) 
  ); 

CREATE TABLE categories ( 
     catid       INT auto_increment, 
     catname     VARCHAR(40) NOT NULL, 
     maincatname VARCHAR(40) NOT NULL, 
     PRIMARY KEY (catid), 
     FOREIGN KEY (maincatname) REFERENCES maincategories(maincatname) 
  ); 

CREATE TABLE products ( 
     refid          INT auto_increment, 
     name           VARCHAR(40) NOT NULL, 
     description    VARCHAR(150) NOT NULL, 
     sellerid       INT NOT NULL, 
     categoryid     INT NOT NULL, 
     estimatedprice NUMERIC(10, 2) NOT NULL, 
     sellingprice   NUMERIC(10, 2) NOT NULL, 
     date           TIMESTAMP DEFAULT CURRENT_TIMESTAMP, 
     PRIMARY KEY (refid), 
     FOREIGN KEY (sellerid) REFERENCES users(userid), 
     FOREIGN KEY (categoryid) REFERENCES categories(catid) 
  ); 

CREATE TABLE offers ( 
     offerid   INT auto_increment, 
     buyerid   INT NOT NULL, 
     productid INT NOT NULL, 
     price     NUMERIC(10, 2) NOT NULL, 
     date      TIMESTAMP DEFAULT CURRENT_TIMESTAMP, 
     PRIMARY KEY (offerid), 
     FOREIGN KEY (buyerid) REFERENCES users(userid), 
     FOREIGN KEY (productid) REFERENCES products(refid) ON DELETE CASCADE 
  ); 

CREATE TABLE soldproducts ( 
     id              INT auto_increment, 
     name            VARCHAR(40) NOT NULL, 
     description     VARCHAR(150) NOT NULL, 
     sellerid        INT NOT NULL, 
     buyerid         INT NOT NULL, 
     categoryid      INT NOT NULL, 
     estimatedprice  NUMERIC(10, 2) NOT NULL, 
     sellingprice    NUMERIC(10, 2) NOT NULL, 
     soldprice       NUMERIC(10, 2) NOT NULL, 
     datetransaction TIMESTAMP DEFAULT CURRENT_TIMESTAMP, 
     PRIMARY KEY (id), 
     FOREIGN KEY (sellerid) REFERENCES users(userid), 
     FOREIGN KEY (buyerid) REFERENCES users(userid), 
     FOREIGN KEY (categoryid) REFERENCES categories(catid) 
  ); 