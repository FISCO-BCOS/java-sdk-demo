#![cfg_attr(not(feature = "std"), no_std)]
#![feature(unboxed_closures, fn_traits)]

use liquid::storage;
use liquid_lang as liquid;

#[liquid::interface(name = auto)]
mod entry {
    extern "liquid" {
        fn getInt(&self, key: String) -> i256;
        fn getUint(&self, key: String) -> u256;
        fn getAddress(&self, key: String) -> Address;
        fn getString(&self, key: String) -> String;

        fn setI256(&mut self, key: String, value: i256);
        fn setU256(&mut self, key: String, value: u256);
        fn setAddress(&mut self, key: String, value: Address);
        fn setString(&mut self, key: String, value: String);
    }
}

#[liquid::interface(name = auto)]
mod condition {
    extern "liquid" {
        fn EQ(&mut self, value1: String, value2: String);
        fn NE(&mut self, value1: String, value2: String);

        fn GT(&mut self, value1: String, value2: i256);
        fn GE(&mut self, value1: String, value2: i256);
        fn LT(&mut self, value1: String, value2: i256);
        fn LE(&mut self, value1: String, value2: i256);
        fn limit(&mut self, lower: i256, upper: i256);
    }
}

#[liquid::interface(name = auto)]
mod entries {
    use super::entry::*;

    extern "liquid" {
        fn get(&self, value: i256) -> Entry;
        fn size(&self) -> i256;
    }
}

#[liquid::interface(name = auto)]
mod table {
    use super::{condition::*, entries::*, entry::*};

    extern "liquid" {
        fn select(&self, condition: Condition) -> Entries;
        fn insert(&mut self, entry: Entry) -> i256;
        fn update(&mut self, entry: Entry, condition: Condition) -> i256;
        fn remove(&mut self, condition: Condition) -> i256;

        fn newEntry(&self) -> Entry;
        fn newCondition(&self) -> Condition;
    }
}

#[liquid::interface(name = auto)]
mod table_factory {
    use super::table::*;

    extern "liquid" {
        fn openTable(&self, name: String) -> Table;
        fn createTable(
            &mut self,
            name: String,
            primary_key: String,
            fields: String,
        ) -> i256;
    }
}

#[liquid::contract]
mod table_test {
    use super::{table_factory::*, *};

    #[liquid(event)]
    struct InsertResult {
        count: i256,
    }

    #[liquid(event)]
    struct UpdateResult {
        count: i256,
    }
    #[liquid(event)]
    struct RemoveResult {
        count: i256,
    }

    #[liquid(storage)]
    struct TableTest {
        table_factory: storage::Value<TableFactory>,
    }

    #[liquid(methods)]
    impl TableTest {
        pub fn new(&mut self) {
            self.table_factory
                .initialize(TableFactory::at("0x1001".parse().unwrap()));
            self.table_factory.createTable(
                String::from("t_test").clone(),
                String::from("name").clone(),
                [
                    String::from("item_price").clone(),
                    String::from("item_name").clone(),
                ]
                .join(","),
            );
        }

        pub fn select(&mut self, name: String) -> (String, i256, String) {
            let table = self
                .table_factory
                .openTable(String::from("t_test").clone())
                .unwrap();
            let mut cond = table.newCondition().unwrap();
            cond.EQ(String::from("name"), name);
            let entries = table.select(cond).unwrap();
            if entries.size().unwrap() < 1.into() {
                return (Default::default(), 0.into(), Default::default());
            }
            let entry = entries.get(0.into()).unwrap();
            return (
                entry.getString(String::from("name").clone()).unwrap(),
                entry.getInt(String::from("item_price").clone()).unwrap(),
                entry.getString(String::from("item_name").clone()).unwrap(),
            );
        }

        pub fn insert(&mut self, name: String, price: i256, item_name: String) -> i256 {
            let mut table = self
                .table_factory
                .openTable(String::from("t_test").clone())
                .unwrap();
            let mut entry = table.newEntry().unwrap();
            entry.setString(String::from("name").clone(), name);
            entry.setI256(String::from("item_price").clone(), price);
            entry.setString(String::from("item_name").clone(), item_name);
            let r = table.insert(entry).unwrap();
            self.env().emit(InsertResult { count: r.clone() });
            return r;
        }

        pub fn update(&mut self, name: String, price: i256, item_name: String) -> i256 {
            let mut table = self
                .table_factory
                .openTable(String::from("t_test").clone())
                .unwrap();
            let mut entry = table.newEntry().unwrap();
            entry.setI256(String::from("item_price").clone(), price);
            entry.setString(String::from("item_name").clone(), item_name);
            let mut cond = table.newCondition().unwrap();
            cond.EQ(String::from("name"), name);
            let r = table.update(entry, cond).unwrap();
            self.env().emit(UpdateResult { count: r.clone() });
            return r;
        }

        pub fn remove(&mut self, name: String) -> i256 {
            let mut table = self
                .table_factory
                .openTable(String::from("t_test").clone())
                .unwrap();
            let mut cond = table.newCondition().unwrap();
            cond.EQ(String::from("name"), name);
            let r = table.remove(cond).unwrap();
            self.env().emit(RemoveResult { count: r.clone() });
            return r;
        }
    }

    #[cfg(test)]
    mod tests {
        use super::*;
        use crate::{entries::*, entry::*, table::*};
        use predicates::prelude::*;

        #[test]
        fn select_works() {
            // EXPECTATIONS SETUP
            let create_table_ctx = TableFactory::createTable_context();
            create_table_ctx.expect().returns(0);

            let open_table_ctx = TableFactory::openTable_context();
            open_table_ctx
                .expect()
                .returns(Table::at(Default::default()));

            let select_ctx = Table::select_context();
            select_ctx
                .expect()
                .returns((true, Entries::at(Default::default())));

            let entries_size_ctx = Entries::size_context();
            entries_size_ctx.expect().returns(1);

            let entries_get_ctx = Entries::get_context();
            entries_get_ctx
                .expect()
                .returns(Entry::at(Default::default()));

            let get_name_ctx = Entry::getString_context();
            get_name_ctx
                .expect()
                .when(predicate::eq(String::from("name")))
                .returns("name1");

            let get_it_name_ctx = Entry::getString_context();
            get_it_name_ctx
                .expect()
                .when(predicate::eq(String::from("item_name")))
                .returns("alice");

            let get_int_ctx = Entry::getInt_context();
            get_int_ctx.expect().returns(2500);

            // TESTS BEGIN
            let mut contract = TableTest::new();

            let (success, name, price, item_name) = contract.select(String::from("cat"));
            assert_eq!(success, true);
            assert_eq!(name, "name1");
            assert_eq!(price, 2500.into());
            assert_eq!(item_name, "alice");
        }
    }
}
