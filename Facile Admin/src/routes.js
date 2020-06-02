import React from "react";
import { Redirect } from "react-router-dom";

// Layout Types
import { DefaultLayout } from "./layouts";

// Route Views
import BlogOverview from "./views/BlogOverview";
import UserProfileLite from "./views/UserProfileLite";
import AddNewPost from "./views/AddNewPost";
import ComponentsOverview from "./views/ComponentsOverview";
import Service from "./views/Service";
import BlogPosts from "./views/BlogPosts";
import AllWorkers from "./views/AllWorkers";
import AllCustomers from "./views/AllCustomers";
import Logout from "./views/Logout";
import AllComplaints from "./views/AllComplaints";

export default [
  {
    path: "/",
    exact: true,
    layout: DefaultLayout,
    component: () => <Redirect to="/user-profile-lite" />
  },
  {
    path: "/blog-overview",
    layout: DefaultLayout,
    component: BlogOverview
  },
  {
    path: "/user-profile-lite",
    layout: DefaultLayout,
    component: UserProfileLite
  },
  {
    path: "/add-new-post",
    layout: DefaultLayout,
    component: AddNewPost
  },
  {
    path: "/components-overview",
    layout: DefaultLayout,
    component: ComponentsOverview
  },
  {
    path: "/worker",
    layout: DefaultLayout,
    component: Service
  },
  {
    path: "/blog-posts",
    layout: DefaultLayout,
    component: BlogPosts
  },
  {
    path: "/all-workers",
    layout: DefaultLayout,
    component: AllWorkers
  },
  {
    path: "/all-customers",
    layout: DefaultLayout,
    component: AllCustomers
  },
  {
    path: "/all-complaints",
    layout: DefaultLayout,
    component: AllComplaints
  },
  {
    path: "/logout",
    layout: DefaultLayout,
    component: Logout
  }
];
