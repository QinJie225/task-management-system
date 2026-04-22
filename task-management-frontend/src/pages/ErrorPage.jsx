import { useRouteError, isRouteErrorResponse } from "react-router-dom";
import { NotFoundPage } from "./NotFoundPage";
import { ForbiddenPage } from "./ForbiddenPage";
import { SomethingWentWrongPage } from "./SomethingWentWrongPage";

export default function ErrorPage() {
  const error = useRouteError();

  if (isRouteErrorResponse(error)) {
    switch (error.status) {
      case 404:
        return <NotFoundPage />;
      case 403:
        return <ForbiddenPage />;
      default:
        return <SomethingWentWrongPage error={error} />;
    }
  }

  return <SomethingWentWrongPage error={error} />;
}
