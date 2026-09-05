import { AuthLayout } from "./AuthLayout";
import { AuthCard } from "./AuthCard";
import { LoginForm } from "./LoginForm";

export default function LoginPage() {
  return (
    <AuthLayout>
      <AuthCard>
        <LoginForm />
      </AuthCard>
    </AuthLayout>
  );
}