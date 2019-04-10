Projet final - Base de données
=======================================

## Sommaire :

1. [Diagramme Entité-Association](#section1)
2. [Modèle relationnel](#section2)
3. [Définition de la base de données](#section3)
	1. [Explications des choix d'implémentation](#section3-1)
	2. [Fonctions](#section3-2)
4. [Ensemble des requêtes en SQL et explications des résultats attendus](#section4)
5. [Guide utilisateur](#section5)

<a id="section1"></a>
## 1. Diagramme Entité-Association

![](https://github.com/JonathanCaspar/ProjetFinalBD/blob/master/modeleEA/modeleEA.png?raw=true)

Tout d&#39;abord, l&#39;application comporte des utilisateurs inscrits « users » qui doivent déclarer leur **nom d&#39;utilisateur, mot de passe et prénom**. Pour des fins de confidentialité, **les numéros de téléphone** et **nom de famille** ne sont pas obligatoires. Lors de la création de l&#39;utilisateur, un **<ins>numéro d&#39;identification unique</ins>** lui sera attitré automatiquement par la base de données.

Une fois enregistré, un utilisateur peut vendre aucun ou plusieurs produits (0 : n) ou faire aucune ou plusieurs offres (0 : n). Quant aux produits, ils peuvent recevoir aucune ou plusieurs (0 : n) offres, n&#39;appartenir qu&#39;à une seule catégorie (1 : 1) et n&#39;avoir qu&#39;un seul vendeur (1 : 1).

Lorsqu&#39;il vend, l&#39;utilisateur crée un nouvel enregistrement dans « products » comprenant **le nom de l&#39;objet, une description, un prix** et sa **catégorie.** Ensuite, l&#39;utilisateur recevra le **prix estimé** par l&#39;expert et pourra accepter ou refuser de vendre au prix indiqué. Lorsque l&#39;offre de l&#39;expert sera acceptée, le produit se verra attribué **la date de mise en vente** , un **<ins>identifiant unique</ins>** et **#l&#39;id vendeur** et **#id catégorie de l&#39;objet.**

Lorsque l&#39;utilisateur fait une offre sur un produit existant, **<ins>l&#39;id unique de l&#39;offre</ins>**,  **le prix offert, #id produit, #id acheteur et la date** seront ajoutés dans la table « offers ». Un produit vendu sera enregistré dans la table « soldProducts » contenant un **id unique** , **son nom**, **description**, **#id du vendeur**, **#id catégorie du produit**, **#id de l&#39;acheteur**, **prix estimé**, **prix de vente**, **prix vendu** et la **date de la transaction**.

Par ailleurs, les catégories principales « mainCategory » sont référencées par leur **<ins>id unique</ins>** et leur **nom.** Elles contiennent une ou plusieurs (1 : n) sous-catégories qui contiennent, à leur tour, ont un **<ins>id unique<ins>** , **un nom** et **#idMaincategory.**

Les sous-catégories « category » ne peuvent avoir qu&#39;une seule (1 : 1) catégorie principale et peuvent contenir aucun ou plusieurs produits (0 : n).

### Légende

- #Clé étrangère
- <ins>Clé principale</ins>
- **Attribut d&#39;une table**

<a id="section2"></a>
## 2. Modèle relationnel

* User(__userID__, username, password, firstname, lastname, phone) 

* Product(__refID__, name, description, #sellerID, #categoryID, estimatedPrice, sellingPrice, date) 

* Offer(__offerID__, #buyerID, #productID, price, date) 

* MainCategory(__mainCatID__, mainCatName) 

* Category(__catID__, #mainCatID, catName) 

* SoldProducts(__id__, name, description, #sellerID, #buyerID, #categoryID, estimatedPrice, sellingPrice, soldPrice, dateTransaction) 

SoldProducts est une table de __log__ conservant l'historique des produits vendus.
  

<a id="section3"></a>
## 3. Définition de la base de données ([DDL.sql](DDL.sql))

<a id="section3-1"></a>
### Explications des choix d'implémentation :
Nous avons décidé de représenter les **acheteurs** et les **vendeurs** en une seule entité (**user**) ayant un ID arbitraire comme clé primaire. Les experts n'ont pas été représenté car nous ne jugions pas cela nécessaire (une simple fenêtre suivant la mise en vente suffit).

Les **produits** ont comme clé primaire standard : un ID généré et ont tous une référence (clé étrangère) vers l'entité du vendeur et de sa catégorie.

Les **catégories** sont classés par **catégories principales** (**maincategories**), et chaque produit fait partie d'une seule catégorie. De plus, on force l'unicité de la paire **(catname et maincatid)** pour éviter les doublons au sein d'une même catégorie principale. (par exemple : une catégorie "Autres" pourrait exister plusieurs fois mais pour des catégories principales différentes seulement.)

Les **offres** sont représentés de la même façon que les produits, elles sont identifiées par un ID généré, et ont tous une référence (clé étrangère) vers l'entité de l'acheteur (celui qui a fait l'offre) et le produit ciblé par l'offre. 
Nous avons choisi d'ajouter un **"ON DELETE CASCADE"** à :
~~~~sql
FOREIGN KEY (productid) REFERENCES products(refid) ON DELETE CASCADE,
~~~~
Afin de faciliter la suppression future des offres pour lesquelles le produit associé n'est plus présent dans la base de données. Ainsi, lorsqu'un produit est supprimé (vente terminée, annulée, etc...) toutes les offres qui y faisaient référence seront aussi supprimées.

La dernière table **soldproducts** nous sert à garder une trace des informations sur les produits vendus.

<a id="section3-2"></a>
### Fonctions :

* Retourne si oui ou non, un mot de passe correspond à l'utilisateur ayant un ID spécifié
~~~~sql
CREATE OR REPLACE FUNCTION check_password(id character varying, pwd character varying)
 RETURNS boolean
 LANGUAGE plpgsql
AS $function$
        DECLARE
        passed BOOLEAN;
BEGIN
        SELECT (password = pwd) INTO passed FROM users WHERE username = id;
        RETURN passed;
END; $function$
~~~~

* Retourne le nom d'une catégorie en fonction de son id
~~~~sql
CREATE OR REPLACE FUNCTION getcategoriesname(id integer)
 RETURNS character varying
 LANGUAGE plpgsql
AS $function$
        DECLARE
        name varchar(40);
BEGIN
        name := (SELECT catname FROM categories WHERE catid = id);
        RETURN name;
END; $function$
~~~~

* Retourne le nom d'une catégorie principale en fonction de son id
~~~~sql
CREATE OR REPLACE FUNCTION getmaincategoriesname(id integer)
 RETURNS character varying
 LANGUAGE plpgsql
AS $function$
        DECLARE
        name varchar(40);
BEGIN
        name := (SELECT catname FROM maincategories WHERE catid = id);
        RETURN name;
END; $function$
~~~~

* Récupérer le nom et prénom concaténé selon l'id fourni :
~~~~sql
CREATE OR REPLACE FUNCTION getUserFullName(id integer)
	RETURNS varchar(60) AS $$
	DECLARE
	userfullname varchar(60);
BEGIN
	userfullname := (SELECT CONCAT(firstname, ', ', lastname) FROM users WHERE userid = id);
	RETURN userfullname;
END; $$
LANGUAGE plpgsql;
~~~~

* Comptabilise le nombre d'offres associées au produit en vente (avec refid)
~~~~sql
CREATE OR REPLACE FUNCTION getOffersCount(refid integer)
	RETURNS integer AS $$
	DECLARE
	offercount integer;
BEGIN
	offercount := (SELECT COUNT(*) FROM offers WHERE productid = refid);
	RETURN offercount;
END; $$
LANGUAGE plpgsql;
~~~~

* Retourne le montant de l'offre la plus elevée pour un produit en vente (avec refid)
~~~~sql
CREATE OR REPLACE FUNCTION getMaxOfferValue(refid integer)
	RETURNS NUMERIC(10, 2) AS $$
	DECLARE
	offercount NUMERIC(10, 2);
BEGIN
	offercount := (SELECT COALESCE(MAX(price), 0) FROM offers WHERE productid = refid);
	RETURN offercount;
END; $$
LANGUAGE plpgsql;
~~~~

<a id="section4"></a>
## 4. L'ensemble des requêtes en SQL et explications des résultats attendus ([LMD.sql](LMD.sql))

A moins de mentionner un autre id d'utilisateur, on suppose pour les requêtes suivantes que l'utilisateur connecté a un id = 18 :

#### 1) Catalogue
* Afficher les catégories principales sur la colonne gauche :
~~~~sql
SELECT * FROM maincategories ORDER BY maincatname;
~~~~

Pour chaque catégorie principale : récupérer les sous-catégories et les ajouter dans la colonne de gauche (exemple avec Meubles : id = 6) :
~~~~sql
SELECT catid, catname FROM categories WHERE maincatid = 6 ORDER BY catname;
~~~~

* En cliquant sur une catégorie : afficher tous les produits (hormis ceux vendus par l'utilisateur lui même)
Exemple avec toutes catégories confondues, on applique un filtre suivant : prix annoncé compris entre 100$ et 250$, prix offert compris entre 100$ et 250$, date de publication comprise entre le 08-04-2019 et le 10-04-2019
~~~~sql
WITH allProducts AS 
(SELECT refid, name, description, sellingprice, getUserFullName(sellerid) AS sellername,
 date, getMaxOfferValue(refid) AS maxoffer, categoryid, estimatedprice 
 FROM products WHERE 
 sellingPrice >= 100 sellingprice <= 250.0 
 AND date >='2019-04-08' AND date <='2019-04-10' 
 AND sellerid <> 18 )
 
SELECT refid, name, description, sellingprice, sellername, date, maxoffer, catname, date, estimatedprice 
FROM allProducts JOIN categories ON categoryid = catid 
WHERE maxoffer >= 100.0 AND maxoffer <= 250.0;
~~~~

#### 2) Mes annonces

* Afficher les produits en vente par l'utilisateur (id=18) en précisant le montant de l'offre maximale pour chaque produit
~~~~sql
WITH allProducts AS 
(SELECT refid, name, description, sellingprice, getUserFullName(sellerid) AS sellername, date, 
 getMaxOfferValue(refid) AS maxoffer, categoryid, estimatedprice  FROM products WHERE sellerid = 18)

SELECT refid, name, description, sellingprice, sellername, date, maxoffer, catname, date, estimatedprice
FROM allProducts JOIN categories ON categoryid = catid;
~~~~

* Afficher les offres liées à l'objet selectionné (exemple produitid = 21)
~~~~sql
SELECT * FROM offers WHERE productid = 21;
~~~~

* Accepter l'offre de l'acheteur (id=25) de 33$ sur le produit "Robe Blanche" (description = nom) classé dans la catégorie "Femmes-Haut" (catid=14) vendu par le vendeur d'id=100 au prix initial de 38.69$ et estimé à 38.69$. On insère le produit vendu dans la table **soldproducts** et on supprime le produit de la table **products** (ainsi que ses autres offres grâce au ON DELETE CASCADE)
~~~~sql
INSERT INTO soldproducts(name, description, sellerid, buyerid, categoryid, estimatedprice, sellingprice, soldprice) 
VALUES ('Robe blanche', 'Robe blanche', 100, 25, 14, 38.69, 38.69, 33);
~~~~

#### 3) Mes achats

* Retourne la liste détaillée des offres actives de l'utilisateur connecté
~~~~sql
SELECT name, getUserFullName(sellerid) AS sellername, sellingprice, price ,offers.date AS dateO 
FROM products JOIN offers ON productid = refid WHERE buyerid = 18;
~~~~

* Retourne la liste détaillée des produits déjà achetés par l'utilisateur (pour lesquels son offre a été acceptée)
~~~~sql
SELECT name, getUserFullName(sellerid) AS sellername, soldprice, datetransaction 
FROM soldproducts WHERE buyerid =18;
~~~~

#### 4) Autres requêtes spéficiques

* Trouve les produits les plus en demande (ceux avec le plus d'offres) par mainCategories
~~~~sql
WITH nombreOfrreParProduit AS 
      (SELECT count(*) AS nbrOffre, productid
          FROM offers GROUP BY productid),
  mainCatWithChildren AS
      (SELECT catid, maincatid, maincatname
          FROM categories NATURAL JOIN maincategories),
  productsWithNbrOffers AS
      (SELECT *
          FROM products JOIN nombreOfrreParProduit
          ON refid = productid),
  mainCatWithProduct AS
    (SELECT  maincatname, refid, name, description, sellerid, 
             categoryid, estimatedprice, sellingprice, nbrOffre
          FROM productsWithNbrOffers JOIN mainCatWithChildren ON catid = categoryid),
  maxByMainCategory AS
      (SELECT  maincatname, MAX(ALL nbrOffre) as nbrOffre
          FROM mainCatWithProduct
          GROUP BY maincatname)
  SELECT * 
      FROM mainCatWithProduct NATURAL JOIN maxByMainCategory;
~~~~
<a id="section5"></a>
## 5. Guide utilisateur
