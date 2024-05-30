// import { useNavigate } from "react-router-dom";
// import { useEffect } from "react";
// import { toast } from "react-toastify";

const Logout = () => {
  // const navigate = useNavigate();

  // useEffect(() => {
  //   const logout = async () => {
  //     try {
  //       const response = await fetch(
  //         `${import.meta.env.VITE_API_URL}/api/logout`,
  //         {
  //           credentials: "include",
  //         }
  //       );

  //       if (response.ok) {
  //         navigate("/");
  //       } else {
  //         console.error("Failed to log out");
  //         toast.error("Failed to log out");
  //       }
  //     } catch (error) {
  //       console.error(error);
  //       toast.error("Failed to log out");
  //     }
  //   };
  //   logout();
  // }, [navigate]);

  return <div>Logging out...</div>;
};

export default Logout;
