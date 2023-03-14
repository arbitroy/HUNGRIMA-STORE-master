CREATE TABLE products (
                          p_id INTEGER PRIMARY KEY AUTOINCREMENT,
                          pname TEXT,
                          price REAL,
                          quantity INTEGER
);

CREATE TABLE transactions (
                              id INTEGER PRIMARY KEY AUTOINCREMENT,
                              pname TEXT,
                              date TEXT,
                              t_type TEXT,
                              quantity INTEGER,
                              s_count INTEGER
);

CREATE TABLE users (
                       id INTEGER PRIMARY KEY AUTOINCREMENT,
                       username TEXT,
                       password TEXT,
                       position TEXT
);

CREATE TABLE supply (
                        supply_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        p_name TEXT,
                        quantity INTEGER,
                        cost REAL,
                        supply_date TEXT
);

CREATE TABLE sales (
                       s_id INTEGER PRIMARY KEY AUTOINCREMENT,
                       custname TEXT,
                       mode_payment TEXT,
                       collection_status TEXT,
                       remarks TEXT,
                       t_amount REAL,
                       sales_date TEXT
);

    CREATE TABLE orders (
                            order_id INTEGER PRIMARY KEY AUTOINCREMENT,
                            pname TEXT,
                            price REAL,
                            amount REAL,
                            quantity INTEGER,
                            s_id INTEGER,
                            FOREIGN KEY (s_id) REFERENCES sales(s_id)
    );

CREATE TABLE customers (
                           cust_id INTEGER PRIMARY KEY AUTOINCREMENT,
                           custname TEXT,
                           phoneno TEXT
);

INSERT INTO products (pname, price, quantity)
VALUES
    ('Product 1', 10.0 , 0),
    ('Product 2', 20.0 , 0),
    ('Product 3', 30.0 , 0);

INSERT INTO transactions (pname, date, t_type, quantity, s_count)
VALUES
    ('Product 1', '2022-01-01', 'Sale', 10, 1),
    ('Product 2', '2022-02-01', 'Purchase', 20, 2),
    ('Product 3', '2022-03-01', 'Sale', 30, 3);

INSERT INTO users (username, password, position)
VALUES
    ('user1', 'password1', 'Manager'),
    ('user2', 'password2', 'Employee'),
    ('user3', 'password3', 'Admin');

INSERT INTO supply (p_name, quantity, cost, supply_date)
VALUES
    ('Product 1', 100, 1000.0, '2022-01-01'),
    ('Product 2', 200, 2000.0, '2022-02-01'),
    ('Product 3', 300, 3000.0, '2022-03-01');

INSERT INTO sales (custname, mode_payment, collection_status, remarks, t_amount, sales_date)
VALUES
    ('Customer 1', 'Cash', 'Paid', 'Good', 100.0, '2022-01-01'),
    ('Customer 2', 'Credit', 'Unpaid', 'Poor', 200.0, '2022-02-01'),
    ('Customer 3', 'Debit', 'Paid', 'Average', 300.0, '2022-03-01');

INSERT INTO orders (pname, price, amount, quantity, s_id)
VALUES
    ('Product 1', 10.0, 100.0, 10, 1),
    ('Product 2', 20.0, 200.0, 20, 2),
    ('Product 3', 30.0, 300.0, 30, 3);

INSERT INTO customers (custname, phoneno)
VALUES
    ('Customer 1', '111-111-1111'),
    ('Customer 2', '222-222-2222'),
    ('Customer 3', '333-333-3333');

-- Create a trigger that updates the quantity field in the products table
-- after an insert on the supply table
CREATE TRIGGER update_products_quantity_supply
AFTER INSERT ON supply
BEGIN
  -- Update the quantity of the product that matches the supply id
  UPDATE products
  SET quantity = quantity + NEW.quantity
  WHERE p_id = NEW.supply_id;
END;


CREATE TRIGGER delete_products_quantity_supply
    AFTER DELETE ON supply
BEGIN
    UPDATE products
    SET quantity = quantity - OLD.quantity
    WHERE pname = OLD.p_name;
END;

CREATE TRIGGER update_product_quantity_supply
    AFTER UPDATE OF quantity ON supply
BEGIN
    UPDATE products
    SET quantity = quantity + NEW.quantity - OLD.quantity
    WHERE pname = NEW.p_name;
END;
