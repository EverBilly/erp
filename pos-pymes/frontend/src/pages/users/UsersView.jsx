import React from 'react';
import { Routes, Route } from 'react-router-dom';
import UsersList from './UsersList';
import UserForm from './UserForm';

const UsersView = () => (
  <Routes>
    <Route path="/" element={<UsersList />} />
    <Route path="/new" element={<UserForm />} />
    <Route path="/edit/:id" element={<UserForm />} />
  </Routes>
);

export default UsersView;
