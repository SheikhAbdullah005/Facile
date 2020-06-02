export default function() {
  return [
    {
      title: "Dashboard Home",
      to: "/add-new-post",
      htmlBefore: '<i class="material-icons">home</i>',
      htmlAfter: ""
    },
    {
      title: "Add New Service",
      htmlBefore: '<i class="material-icons">add_circle</i>',
      to: "/add-new-post",
    },
    {
      title: "Verified Workers",
      htmlBefore: '<i class="material-icons">vertical_split</i>',
      to: "/all-workers",
    },
    {
      title: "Pending Workers",
      htmlBefore: '<i class="material-icons">vertical_split</i>',
      to: "/worker",
    },
    {
      title: "All Customers",
      htmlBefore: '<i class="material-icons">vertical_split</i>',
      to: "/all-customers",
    },
    {
      title: "All Services",
      htmlBefore: '<i class="material-icons">horizontal_split</i>',
      to: "/blog-posts",
    },
    {
      title: "Complains",
      htmlBefore: '<i class="material-icons">help</i>',
      to: "/all-complaints",
    },
    {
      title: "Logout",
      htmlBefore: '<i class="material-icons">remove_circle</i>',
      to: "/logout",
    },
  ];
}

///components-overview
///tables