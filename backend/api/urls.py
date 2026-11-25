from django.urls import path, include
from rest_framework.routers import DefaultRouter
from rest_framework_simplejwt.views import TokenObtainPairView, TokenRefreshView
from .views import (
    UserViewSet,
    ComplaintViewSet,
    DepartmentViewSet,
    NotificationViewSet,
    AssignmentViewSet,
    MyTokenObtainPairView,
    AnnouncementViewSet
)

router = DefaultRouter()
router.register(r'users', UserViewSet)
router.register(r'complaints', ComplaintViewSet)
router.register(r'departments', DepartmentViewSet)
router.register(r'notifications', NotificationViewSet, basename='notification')
router.register(r'assignments', AssignmentViewSet)
router.register(r'announcements', AnnouncementViewSet)

urlpatterns = [
    path('', include(router.urls)),
    path('auth/login/', MyTokenObtainPairView.as_view(), name='token_obtain_pair'),
    path('auth/refresh/', TokenRefreshView.as_view(), name='token_refresh'),
    path('auth/register/', UserViewSet.as_view({'post': 'create'}), name='register'),
]
