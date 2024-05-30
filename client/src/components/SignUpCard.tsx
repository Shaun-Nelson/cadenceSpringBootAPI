import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useSignupMutation } from "../slices/usersApiSlice";
import { toast } from "react-toastify";

const SignUpCard = () => {
  const [username, setUsername] = useState<string>("");
  const [password, setPassword] = useState<string>("");
  const [confirmPassword, setConfirmPassword] = useState<string>("");

  const navigate = useNavigate();
  const [signup] = useSignupMutation();

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    try {
      if (password !== confirmPassword) {
        toast.error("Passwords don't match");
      } else {
        await signup({ username, password }).unwrap();

        navigate("/login");
      }
    } catch (error) {
      console.error(error);
      toast.error("Failed to sign up");
    }
  };

  return (
    <div className='flex-container'>
      <form className='form-container' onSubmit={handleSubmit}>
        <h3 className='form-header'>Sign Up</h3>
        <input
          type='text'
          placeholder='Username'
          value={username}
          onChange={(e) => setUsername(e.target.value)}
        />
        <input
          type='password'
          placeholder='Password'
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        <input
          type='password'
          placeholder='Confirm Password'
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
        />
        <button type='submit' style={{ marginTop: "1em" }}>
          Sign Up
        </button>

        <p style={{ marginTop: "1em" }}>
          Already have an account? <Link to={"/login"}>Login</Link>
        </p>
      </form>
    </div>
  );
};

export default SignUpCard;
